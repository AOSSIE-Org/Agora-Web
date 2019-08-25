package models

import io.swagger.annotations.{ApiModel, ApiModelProperty}
import play.api.libs.json.{Format, JsPath, Json, OFormat}
import play.api.libs.functional.syntax._

@ApiModel(description = "Voter data", value = "Voter")
case class Voter (@ApiModelProperty(value = "Voters name", required = true)name: String,
                  @ApiModelProperty(value = "Voters email", required = true)hash: String)

object Voter{
  implicit val voterFormat : Format[Voter] = (
    (JsPath \ "name").format[String] and
      (JsPath \ "hash").format[String]
    )(Voter.apply, unlift(Voter.unapply))
}
