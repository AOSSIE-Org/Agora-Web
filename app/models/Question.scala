package models

import io.swagger.annotations.{ApiModel, ApiModelProperty}
import play.api.libs.json.{Format, JsPath, Json, OFormat}
import play.api.libs.functional.syntax._

@ApiModel(description = "Security Question", value = "Question")
case class Question (@ApiModelProperty(value = "Security Question", required = true, example = "What is your pet name?")question: String,
                  @ApiModelProperty(value = "Answer", required = true, example = "Snow")answer: String)
object Question {
  implicit val voterFormat : Format[Question] = (
    (JsPath \ "question").format[String] and
      (JsPath \ "answer").format[String]
    )(Question.apply, unlift(Question.unapply))
}
