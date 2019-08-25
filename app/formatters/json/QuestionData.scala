package formatters.json

import io.swagger.annotations.{ApiModel, ApiModelProperty}
import play.api.libs.json._

@ApiModel(description = "Security Question", value = "Question")
case class QuestionData (
                      @ApiModelProperty(value = "crypto", required = true) crypto: String,
                      @ApiModelProperty(value = "Security Question", required = true)question: String,
                      @ApiModelProperty(value = "Answer", required = true)answer: String)

object QuestionData {
  implicit val userDataFormat = Json.format[QuestionData]
}