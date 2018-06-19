package models

import agora.model.Candidate
import io.swagger.annotations.{ApiModel, ApiModelProperty}
import play.api.libs.json.{Format, JsPath, Json, OFormat}
import play.api.libs.functional.syntax._

@ApiModel(description = "Winner object")
case class Winner(
  @ApiModelProperty(value = "Candidate", required = true)candidate : Candidate,
  @ApiModelProperty(value = "Score", required = true)score : Score
)

object Winner {
  implicit lazy val candidateFormat : Format[Candidate] = (
    (JsPath \ "name").format[String] and
      (JsPath \ "id").formatNullable[Int] and
      (JsPath \ "party").formatNullable[String]
    )(Candidate.apply, unlift(Candidate.unapply))

  implicit lazy val winnerFormat : Format[Winner] = (
    (JsPath \ "candidate").format[Candidate] and
      (JsPath \ "score").format[Score]
    )(Winner.apply, unlift(Winner.unapply))
}
