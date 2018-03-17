package api

import akka.http.scaladsl.model.headers.HttpChallenges
import akka.http.scaladsl.server.AuthenticationFailedRejection.{CredentialsMissing, CredentialsRejected}
import akka.http.scaladsl.server.directives.AuthenticationDirective
import akka.http.scaladsl.server.{AuthenticationFailedRejection, Directive1, Directives}
import authenticator.Failure
import authenticator.exception.AuthenticatorException
import model.User
import services.{Authenticated, AuthenticationService, WaitingForApproval}

import scala.concurrent.{ExecutionContext, Future}


class AuthenticationDirectives(authService: AuthenticationService) extends Directives {

  val realm = "app_auth"

  def authenticator(token: String)(implicit executionContext: ExecutionContext): Future[AuthenticationResult[User]] = {
    authService.authenticate(token).map {
      case Authenticated(user) => Right(user)
      case WaitingForApproval(mateName) => throw new AuthenticatorException(s"Waiting for $mateName to validate you.")
      case f: Failure[_] => throw new AuthenticatorException(s"Unauthorized: ${f.cause.getMessage}")
      case any => throw new AuthenticatorException(s"Unauthorized: ${any.getClass.getName}")
    }
  }

  def authenticated(implicit executionContext: ExecutionContext): AuthenticationDirective[User] =
    parameter('token.?).flatMap {
      case Some(token) =>
        onSuccess(authenticator(token)).flatMap {
          case Right(s) => provide(s)
          case Left(challenge) =>
            reject(AuthenticationFailedRejection(CredentialsRejected, challenge)): Directive1[User]
        }
      case None =>
        reject(AuthenticationFailedRejection(CredentialsMissing, HttpChallenges.basic(realm))): Directive1[User]
    }

}
