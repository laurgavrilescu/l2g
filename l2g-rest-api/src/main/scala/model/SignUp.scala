package model

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.unmarshalling.Unmarshaller
import io.swagger.annotations.{ApiModel, ApiModelProperty}
import play.api.libs.json.{Json, OFormat}

@ApiModel(description = "Sign up object")
case class SignUp(@ApiModelProperty(value = "User name", required = true, example = "john.doe") identifier: String,
                  @ApiModelProperty(value = "Password", required = true, example = "<password>") password: String,
                  @ApiModelProperty(value = "E-mail", required = true, example = "john.doe@test.com") email: String,
                  @ApiModelProperty(value = "Mate username", required = false, example = "jane.doe") mateName: Option[String],
                  @ApiModelProperty(value = "Activation code", required = false, example = "<code>") activationCode: Option[String])

object SignUpJson {
  implicit val format: OFormat[SignUp] = Json.format[SignUp]

  implicit val signUpJsonUnmarshaller: Unmarshaller[HttpEntity, SignUp] = Unmarshaller
    .stringUnmarshaller
    .forContentTypes(ContentTypes.`application/json`)
    .map(d => Json.parse(d).as[SignUp])
}
