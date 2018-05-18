package formatters.json

import io.swagger.annotations.{ApiModel, ApiModelProperty}
import play.api.libs.json.Json

@ApiModel(description = "Password data", value = "Password")
case class PasswordData (@ApiModelProperty(value = "New Password", required = true)password: String)

object PasswordData {
  implicit val passwordDataFormat = Json.format[PasswordData]
}