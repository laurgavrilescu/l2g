package model

import io.swagger.annotations.{ApiModel, ApiModelProperty}
import json.DateTimeFormatJson
import org.joda.time.DateTime
import play.api.libs.json.{Json, OFormat}

@ApiModel(description = "Token object")
case class Token(@ApiModelProperty(value = "token value", readOnly = true) token: String,
                 @ApiModelProperty(value = "expiry date", readOnly = true) expiresOn: DateTime)

object TokenJson {
  import DateTimeFormatJson._

  implicit val tokenFormat: OFormat[Token] = Json.format[Token]
}
