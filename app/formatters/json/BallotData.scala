package formatters.json

import io.swagger.annotations.{ApiModel, ApiModelProperty}
import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath}

@ApiModel(description = "Ballot data", value = "Ballot")
case class BallotData(
   @ApiModelProperty(value = "Election ID", required = true)id: String,
   @ApiModelProperty(value = "Ballot data", required = true)ballotInput: String,
   @ApiModelProperty(value = "Voters pass code", required = true)passCode : String)

object BallotData {
  implicit val ballotDataFormat : Format[BallotData] = (
    (JsPath \ "id").format[String] and
      (JsPath \ "ballotInput").format[String] and
      (JsPath \ "passCode").format[String]
    )(BallotData.apply, unlift(BallotData.unapply))

}
