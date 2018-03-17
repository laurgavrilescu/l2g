package json

import authenticator.TokenAuthenticator
import model.LoginEntity
import play.api.libs.json._

object TokenAuthenticatorJson {
  import DateTimeFormatJson._

  implicit val loginEntityFormat: OFormat[LoginEntity] = Json.format[LoginEntity]

  implicit val tokenAuthFormat: OFormat[TokenAuthenticator] = Json.format[TokenAuthenticator]
}