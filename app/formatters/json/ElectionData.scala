package formatters.json

import java.util.Date

import io.swagger.annotations.{ApiModel, ApiModelProperty}
import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath}

@ApiModel(description = "Election data", value = "Election")
case class ElectionData(
   @ApiModelProperty(value = "Election ID", required = false)id: Option[String],
   @ApiModelProperty(value = "Election name", required = true)name: String,
   @ApiModelProperty(value = "Election description", required = true)description: String,
   @ApiModelProperty(value = "Creators name", required = true)creatorName: String,
   @ApiModelProperty(value = "Creators email", required = true)creatorEmail: String,
   @ApiModelProperty(value = "Election candidates", required = true)candidates: List[String],
   @ApiModelProperty(value = "Election ballot visibility status", required = true)ballotVisibility: String,
   @ApiModelProperty(value = "Voters list should be made visible?", required = true)voterListVisibility:  Boolean,
   @ApiModelProperty(value = "Election start date", required = true)startingDate: Date,
   @ApiModelProperty(value = "Election end date", required = true)endingDate: Date,
   @ApiModelProperty(value = "Invite voters?", required = true)isInvite: Boolean,
   @ApiModelProperty(value = "Get real time result?", required = true)isRealTime: Boolean,
   @ApiModelProperty(value = "Voting algorithm", required = true)votingAlgo: String,
   @ApiModelProperty(value = "Number of vacancies", required = true)noVacancies : Int )

object ElectionData {
  implicit val electionDataFormat : Format[ElectionData] = (
    (JsPath \ "id").formatNullable[String] and
      (JsPath \ "name").format[String] and
      (JsPath \ "description").format[String] and
      (JsPath \ "creatorName").format[String] and
      (JsPath \ "creatorEmail").format[String] and
      (JsPath \ "candidates").format[List[String]] and
      (JsPath \ "ballotVisibility").format[String] and
      (JsPath \ "voterListVisibility").format[Boolean] and
      (JsPath \ "startingDate").format[Date] and
      (JsPath \ "endingDate").format[Date] and
      (JsPath \ "isInvite").format[Boolean] and
      (JsPath \ "isRealTime").format[Boolean] and
      (JsPath \ "votingAlgo").format[String] and
      (JsPath \ "noVacancies").format[Int]
    )(ElectionData.apply, unlift(ElectionData.unapply))

}