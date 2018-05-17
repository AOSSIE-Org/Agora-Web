package formatters.json

import io.swagger.annotations.{ApiModel, ApiModelProperty}
import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath}

@ApiModel(description = "Election status data", value = "Status")
case class StatusData (
  @ApiModelProperty(value = "Election ID", required = true)id: String,
  @ApiModelProperty(value = "Election status", required = true, allowableValues = "Completed,Active,Not yet started")status: String)

object StatusData {
  implicit val statusDataFormat : Format[StatusData] = (
    (JsPath \ "id").format[String] and
      (JsPath \ "status").format[String]
    )(StatusData.apply, unlift(StatusData.unapply))
}