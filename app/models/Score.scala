package models
import io.swagger.annotations.{ApiModel, ApiModelProperty}
import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath, Json, OFormat}

@ApiModel(description = "Score object")
case class Score(
  @ApiModelProperty(value = "Score numerator", required = true)numerator : Int,
  @ApiModelProperty(value = "Score denominator", required = true)denominator : Int
)

object Score {
  implicit val scoreFormat : Format[Score] = (
    (JsPath \ "numerator").format[Int] and
      (JsPath \ "denominator").format[Int]
    )(Score.apply, unlift(Score.unapply))
}