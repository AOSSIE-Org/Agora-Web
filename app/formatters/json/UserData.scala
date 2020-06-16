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
                      @ApiModelProperty(value = "Two Factor Authentication", required = true)twoFactorAuthentication: Boolean,
                      @ApiModelProperty(value = "The Auth Token", required = false)authToken: Option[Token],
                      @ApiModelProperty(value = "The Refresh Token", required = false)refreshToken: Option[Token],
                      @ApiModelProperty(value = "Trusted Device", required = false)trustedDevice: Option[String])

object UserData {
  implicit val userDataFormat = Json.format[UserData]
}