package formatters.json

import io.swagger.annotations.{ApiModel, ApiModelProperty}
import play.api.libs.json.Json

@ApiModel(description = "New Avatar URL", value = "URL")
case class AvatarURLData (@ApiModelProperty(value = "New Avatar URL", required = true)url: String)

object AvatarURLData {
  implicit val avatarDataFormat = Json.format[AvatarURLData]
}