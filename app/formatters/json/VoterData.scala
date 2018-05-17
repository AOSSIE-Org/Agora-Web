package formatters.json

import io.swagger.annotations.{ApiModel, ApiModelProperty}
import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath}

@ApiModel(description = "Voter data", value = "Voter")
case class VoterData(
  @ApiModelProperty(value = "Election ID", required = true)id: String,
  @ApiModelProperty(value = "Voters name", required = true)name: String,
  @ApiModelProperty(value = "Voters email", required = true)email: String )

object VoterData {
  implicit val voterDataFormat : Format[VoterData] = (
    (JsPath \ "id").format[String] and
      (JsPath \ "name").format[String] and
      (JsPath \ "email").format[String]
    )(VoterData.apply, unlift(VoterData.unapply))
}
