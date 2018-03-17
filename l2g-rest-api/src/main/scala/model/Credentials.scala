package model

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.unmarshalling.Unmarshaller
import io.swagger.annotations.{ApiModel, ApiModelProperty}
import play.api.libs.json.{Json, OFormat}

@ApiModel(description = "Credential object")
case class Credentials(@ApiModelProperty(value = "User name", required = true, example = "john.doe")identifier: String,
                       @ApiModelProperty(value = "Password", required = true, example = "pass")password: String)

object CredentialsJson {
  implicit val format: OFormat[Credentials] = Json.format[Credentials]

  implicit val credentialsJsonUnmarshaller: Unmarshaller[HttpEntity, Credentials] = Unmarshaller
    .stringUnmarshaller
    .forContentTypes(ContentTypes.`application/json`)
    .map(d => Json.parse(d).as[Credentials])

}
