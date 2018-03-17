package security

import authenticator.{To, TokenAuthenticator}
import play.api.libs.json.Json
import json.TokenAuthenticatorJson._
import model.Token

import scala.concurrent.Future

case class ToToken(secretKey: String) extends To[TokenAuthenticator, Future[Token]] {
  override def write(authenticator: TokenAuthenticator): Future[Token] =
    Future.successful(Token(AES.encrypt(Json.toJson(authenticator).toString(), secretKey), authenticator.expirationDateTime))
}
