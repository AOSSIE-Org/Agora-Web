package models

import play.api.libs.json.{Format, JsPath, Json, OFormat}
import play.api.libs.functional.syntax._

case class Ballot(
   voteBallot :String,
   hash : String
)

object Ballot {
  implicit val ballotFormat : Format[Ballot] = (
    (JsPath \ "voteBallot").format[String] and
      (JsPath \ "hash").format[String]
    )(Ballot.apply, unlift(Ballot.unapply))

}