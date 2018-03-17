package security

import FromToken._
import authenticator.exception.AuthenticatorException
import authenticator.{Authenticator, From, TokenAuthenticator}
import play.api.libs.json.Json
import json.TokenAuthenticatorJson._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

final case class FromToken(secretKey: String)(implicit ex: ExecutionContext) extends From[String, Future[Authenticator]] {

  override def read(token: String): Future[Authenticator] = Future.successful(
    Try(Json.parse(AES.decrypt(token, secretKey)).asOpt[TokenAuthenticator]).getOrElse(
      throw new AuthenticatorException(InvalidToken.format(token, this.getClass.getName))
    )
  ).map(_.getOrElse(
    throw new AuthenticatorException(MissingAuthenticatorMessage.format(token, this.getClass.getName))
  ))
}

object FromToken {
  val MissingAuthenticatorMessage: String = "Cannot get authenticator for token `%s` from `%s` reader"
  val InvalidToken: String = "Invalid token `%s` from `%s` reader"
}