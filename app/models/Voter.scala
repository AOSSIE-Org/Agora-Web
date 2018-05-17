package models

import play.api.libs.json.{Format, JsPath, Json, OFormat}
import play.api.libs.functional.syntax._

case class Voter (name : String, email : String)

object Voter{
  implicit val voterFormat : Format[Voter] = (
    (JsPath \ "name").format[String] and
      (JsPath \ "email").format[String]
    )(Voter.apply, unlift(Voter.unapply))
}
