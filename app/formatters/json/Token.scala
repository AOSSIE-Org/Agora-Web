package formatters.json

import io.swagger.annotations.{ApiModel, ApiModelProperty}
import org.joda.time.DateTime
import play.api.libs.json._

import scala.util.{Failure, Success, Try}

/**
  * This class represent token
  *
  * @param token     Id of token
  * @param expiresOn The expiration time
  */
@ApiModel(description = "Token object")
case class Token(
                  @ApiModelProperty(value = "token value", readOnly = true) token: String,
                  @ApiModelProperty(value = "expiry date", readOnly = true) expiresOn: DateTime)

object Token {

  implicit val jodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss'Z'")
  implicit val jodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss'Z'")

  implicit object TokenWrites extends OWrites[Token] {
    def writes(token: Token): JsObject = {
      val json = Json.obj(
        "token" -> token.token,
        "expiresOn" -> token.expiresOn.toString
      )

      json
    }
  }

  implicit object TokenReads extends Reads[Token] {
    override def reads(json: JsValue): JsResult[Token] = json match {
      case token: JsObject =>
        Try {
          val t = (token \ "token").as[String]
          val expires = (token \ "expiresOn").as[DateTime]

          JsSuccess(
            new Token(t, expires)
          )
        } match {
          case Success(value) => value
          case Failure(cause) => JsError(cause.getMessage)
        }
      case _ => JsError("expected.jsobject")
    }
  }

}
