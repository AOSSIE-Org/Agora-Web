package models

import play.api.libs.json.{Format, JsPath, Json, OFormat}
import play.api.libs.functional.syntax._
import reactivemongo.api.bson.{BSONDocumentHandler, Macros}

case class Ballot(
   voteBallot :String,
   hash : String
)

object Ballot {
  implicit val ballotFormat : OFormat[Ballot] = Json.format[Ballot]

  implicit val handler: BSONDocumentHandler[Ballot] = Macros.handler[Ballot]
}