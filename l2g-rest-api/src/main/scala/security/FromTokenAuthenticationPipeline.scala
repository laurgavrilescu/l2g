package security

import authenticator._
import authenticator.validator.ExpirationValidator
import model.{LoginEntity, User}
import services.{Authenticated, UserService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import FromTokenAuthenticationPipeline._

class FromTokenAuthenticationPipeline(reads: From[String, Future[Authenticator]],
                                                     userService: UserService,
                                                     override val validators: Set[Validator] = Set(ExpirationValidator))
                                                             (implicit ec: ExecutionContext) extends AuthenticationPipeline[String, User] {

  val id: String = "authentication"

  override def read(token: String): Future[State[User]] = reads.read(token).toState

  override val identityReader: LoginEntity => Future[Option[User]] = userService.retrieve

  def isValid(authenticator: Authenticator)(implicit ec: ExecutionContext): Future[Boolean] = {
    Future.sequence(validators.map(_.isValid(authenticator))).map(!_.exists(!_))
  }

  override def toState(authenticator: Authenticator)(implicit ec: ExecutionContext): Future[State[User]] = {
    isValid(authenticator).flatMap {
      case false => Future.successful(TokenHasExpiredOrTimedOut(TokenIsNotValid.format(id)))
      case true => identityReader(authenticator.loginEntity).map {
        case None => MissingIdentity(UserDoesNotExist.format(id))
        case Some(user) => Authenticated(user)
      }
    }
  }
}
object FromTokenAuthenticationPipeline {
  val TokenIsNotValid = "[%s] Token has expired or timed out"
  val UserDoesNotExist = "[%s] Identity does not exist"
}

case class MissingIdentity(error: String) extends Failure[User](error)

case class TokenHasExpiredOrTimedOut(error: String) extends Failure[User](error)
