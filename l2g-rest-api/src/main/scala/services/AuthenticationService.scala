package services

import java.security.MessageDigest
import java.util.UUID

import dao.AuthenticationInfo
import model._
import repository.SimpleAuthenticationRepository

import scala.concurrent.{ExecutionContext, Future}
import authenticator.{Failure, State, TokenAuthenticator}
import org.joda.time.{DateTime, DateTimeZone}
import org.mindrot.jbcrypt.BCrypt
import security.{ActivationCode, FromToken, FromTokenAuthenticationPipeline, ToToken}

import scala.concurrent.duration.FiniteDuration
import AuthenticationService._

class AuthenticationService(id: String,
                            userService: UserService,
                            repository: SimpleAuthenticationRepository[AuthenticationInfo],
                            tokenExpiration: FiniteDuration,
                            secretKey: String)
                           (implicit ec: ExecutionContext) {


  def toLoginEntity(credentials: Credentials): Future[LoginEntity] = Future.successful(LoginEntity("authentication", credentials.identifier))

  def toLoginEntity(identifier: String): Future[LoginEntity] = Future.successful(LoginEntity("authentication", identifier))

  def toLoginEntity(signUp: SignUp): Future[LoginEntity] = Future.successful(LoginEntity("authentication", signUp.identifier))

  def authenticate(token: String): Future[State[User]] = {
    new FromTokenAuthenticationPipeline(FromToken(secretKey), userService).read(token) flatMap {
      case Authenticated(user) =>
        checkIfUserIsActive(user)
      case any => Future.successful(any)
    }
  }

  def authenticate(credentials: Credentials): Future[State[User]] = {
    toLoginEntity(credentials).flatMap { loginEntity =>
      authenticate(loginEntity, credentials.password).flatMap {
        case Authenticated(user) =>
          checkIfUserIsActive(user)
        case any => Future.successful(any)
      }
    }
  }

  private def checkIfUserIsActive(user: User): Future[State[User]] = {
    if (!user.requiresMate) {
      Future.successful(Authenticated(user))
    }
    else {
      if (user.activated) {
        checkMate(user)
      }
      else {
        user.mateName.map(mateName => Future.successful(WaitingForApproval(mateName)))
          .getOrElse(Future.successful(BlockedUser))
      }
    }
  }

  private def checkMate(user: User): Future[State[User]] = {
    user.mateName match {
      case Some(mateId) =>
        toLoginEntity(mateId) flatMap {
          mateLoginEntity =>
            userService.retrieve(mateLoginEntity) flatMap {
              case None =>
                Future.successful(NoMate(MateDoesNotExist.format(id, mateLoginEntity.uid)))
              case Some(mate) =>
                mate.lastIssuedToken.map { token =>
                  new FromToken(secretKey).read(token).map { auth =>
                    if (auth.isValid) {
                      Authenticated(user)
                    } else {
                      MateNotLoggedIn(MateIsNotLoggedIn.format(id, mateLoginEntity.uid))
                    }
                  }
                }.getOrElse(Future.successful(MateNotLoggedIn(MateIsNotLoggedIn.format(id, mateLoginEntity.uid))))
            }
        }
      case None => Future.successful(NoMate(UserHasNoMate.format(id, user.name)))
    }
  }

  private def passwordMatches(passwordInfo: AuthenticationInfo, suppliedPassword: String): Boolean = {
    BCrypt.checkpw(suppliedPassword, passwordInfo.password)
  }

  private def authenticationInfo(signUp: SignUp): AuthenticationInfo = {
    val salt = BCrypt.gensalt()
    val password = BCrypt.hashpw(signUp.password, BCrypt.gensalt())
    AuthenticationInfo(password, Some(salt))
  }

  private def authenticate(loginEntity: LoginEntity, password: String): Future[State[User]] = {
    repository.find(loginEntity).flatMap {
      case Some(authenticationInfo: AuthenticationInfo) =>
        if (passwordMatches(authenticationInfo, password)) {
          userService.retrieve(loginEntity).flatMap {
            case None =>
              Future.successful(NotFound(UserDoesNotExist.format(id, loginEntity.uid)))
            case Some(user) =>
              Future.successful(Authenticated(user))
          }
        }
        else {
          Future.successful(InvalidPassword(PasswordDoesNotMatch.format(id)))
        }
      case None => Future.successful(NotFound(UserDoesNotExist.format(id, loginEntity.uid)))
    }
  }

  def createAuthenticator(credentials: Credentials): Future[TokenAuthenticator] = {
    toLoginEntity(credentials) map {
      loginEntity =>
        val now = new DateTime(DateTimeZone.UTC)
        TokenAuthenticator(
          loginEntity,
          now,
          now.plus(tokenExpiration.toMillis),
          None
        )
    }
  }

  def createToken(signUp: SignUp): Future[Token] = {
    val credentials = Credentials(signUp.identifier, signUp.password)
    createAuthenticator(credentials) flatMap { tokenAuthenticator => ToToken(secretKey).write(tokenAuthenticator) }
  }

  def createToken(user: User, credentials: Credentials): Future[Token] = {
    createAuthenticator(credentials) flatMap {
      tokenAuthenticator =>
        user.copy()
        ToToken(secretKey).write(tokenAuthenticator) map {
          token =>
            val updatedUser = user.copy(lastIssuedToken = Some(token.token))
            userService.save(updatedUser)
            token
        }
    }
  }

  def signUp(signUp: SignUp): Future[State[User]] = {
    toLoginEntity(signUp) flatMap { loginEntity =>
      repository.find(loginEntity).flatMap {
        case None =>
          val authInfo = authenticationInfo(signUp)
          signUp.activationCode match {
            case None =>
              signUp.mateName.map { mateName =>
                repository.add(loginEntity, authInfo) flatMap {
                  _ =>
                    val user = User(signUp.identifier, signUp.email, signUp.mateName, None, requiresMate = true, activated = false)
                    userService.save(user) map (_ => WaitingForApproval(mateName))
                }
              }.getOrElse(Future.successful(NoMate(MateNotProvided.format(id))))
            case Some(code) => if (!ActivationCode.valid(code)) {
              Future.successful(ActivationCodeInvalid(InvalidCode.format(id, code)))
            } else {
              repository.add(loginEntity, authInfo) flatMap {
                _ =>
                  createToken(signUp) flatMap {
                    token =>
                      val user = User(signUp.identifier, signUp.email, None, Some(token.token), requiresMate = false, activated = true)
                      userService.save(user) map (_ => Authenticated(user))
                  }
              }
            }
          }

        case Some(_) => Future.successful(UserExists(UserAlreadyExists.format(id, signUp.identifier)))
      }
    }
  }
}

object AuthenticationService {
  val PasswordDoesNotMatch = "[%s] Passwords does not match"
  val InvalidCode = "[%s] Invalid code %s"
  val UserDoesNotExist = "[%s] User does not exist: %s"
  val MateDoesNotExist = "[%s] Mate does not exist: %s"
  val UserHasNoMate = "[%s] User has no mate: %s"
  val MateIsNotLoggedIn = "[%s] Mate is not logged in: %s"
  val MateNotProvided = "[%s] You did not provide any mate"
  val UserAlreadyExists = "[%s] User already exists: %s"
  val PasswordNotFound = "[%s] Could not find password for given login entity: %s"
}


case class Authenticated(user: User) extends State[User]

case class WaitingForApproval(mateName: String) extends State[User]

case object BlockedUser extends State[User]

case class UserExists(error: String) extends Failure[User](error)


case class ActivationCodeInvalid(error: String) extends Failure[User](error)

case class InvalidPassword(error: String) extends Failure[User](error)

case class MateNotLoggedIn(error: String) extends Failure[User](error)

case class NoMate(error: String) extends Failure[User](error)

case class NotFound(error: String) extends Failure[User](error)


