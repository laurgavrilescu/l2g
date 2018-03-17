package model

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.unmarshalling.Unmarshaller
import io.swagger.annotations.{ApiModel, ApiModelProperty}
import play.api.libs.json.{Json, OFormat}

sealed trait Permission

@ApiModel(description = "Authentication permission")
case class AuthenticationPermission(@ApiModelProperty(value = "User name to give permission for", required = true, example = "john.doe") forUserName: String)

object AuthenticationPermissionJson {
  implicit val authenticationPermissionFormat: OFormat[AuthenticationPermission] = Json.format[AuthenticationPermission]

  implicit val authenticationPermissionJsonUnmarshaller: Unmarshaller[HttpEntity, AuthenticationPermission] = Unmarshaller
    .stringUnmarshaller
    .forContentTypes(ContentTypes.`application/json`)
    .map(d => Json.parse(d).as[AuthenticationPermission])
}
