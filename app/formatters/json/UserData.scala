package formatters.json

import io.swagger.annotations.{ApiModel, ApiModelProperty}
import play.api.libs.json._

@ApiModel(description = "User data", value = "User")
case class UserData (
                      @ApiModelProperty(value = "User name", required = true)username: String,
                      @ApiModelProperty(value = "User email", required = true)email: String,
                      @ApiModelProperty(value = "User first name", required = true)firstName: String,
                      @ApiModelProperty(value = "User last name", required = true)lastName: String,
                      @ApiModelProperty(value = "Avatar URL", required = false)avatarURL: Option[String],
                      token: Option[Token])

object UserData {
  implicit val userDataFormat = Json.format[UserData]
}