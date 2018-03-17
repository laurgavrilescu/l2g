package model

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.unmarshalling.Unmarshaller
import io.swagger.annotations.{ApiModel, ApiModelProperty}
import play.api.libs.json.{Json, OFormat}

case class Secret(userName: String, text: String, allowedUsers: Set[String])

@ApiModel(description = "Secret text")
case class SecretText(@ApiModelProperty(value = "Secret text", readOnly = true, example = "mySecretText") value: String)

@ApiModel(description = "Secret share")
case class SecretShare(@ApiModelProperty(value = "User to share the secret", readOnly = true, example = "john.doe") secretText: String,
                       @ApiModelProperty(value = "User to share the secret", readOnly = true, example = "john.doe") userName: String)

case class SecretList(secrets: Seq[String])

object SecretJson {
  implicit val secretFormat: OFormat[Secret] = Json.format[Secret]

  implicit val secretTextFormat: OFormat[SecretText] = Json.format[SecretText]

  implicit val secretShareFormat: OFormat[SecretShare] = Json.format[SecretShare]

  implicit val secretList: OFormat[SecretList] = Json.format[SecretList]

  implicit val secretTextJsonUnmarshaller: Unmarshaller[HttpEntity, SecretText] = Unmarshaller
    .stringUnmarshaller
    .forContentTypes(ContentTypes.`application/json`)
    .map(d => Json.parse(d).as[SecretText])

  implicit val secretShareJsonUnmarshaller: Unmarshaller[HttpEntity, SecretShare] = Unmarshaller
    .stringUnmarshaller
    .forContentTypes(ContentTypes.`application/json`)
    .map(d => Json.parse(d).as[SecretShare])
  
}