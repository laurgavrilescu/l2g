package model

import io.swagger.annotations.ApiModel
import play.api.libs.json.{Json, OFormat}

case class User(name: String,
                email: String,
                mateName: Option[String],
                lastIssuedToken: Option[String],
                requiresMate: Boolean,
                activated: Boolean) extends Identity

@ApiModel(description = "List of user names")
case class UserNameList(users: Seq[String])

case class UserMessage(message: String)

object UserJson {
  implicit val unlFormat: OFormat[UserNameList] = Json.format[UserNameList]

  implicit val ucFormat: OFormat[UserMessage] = Json.format[UserMessage]
}

