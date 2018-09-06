package formatters.json

import play.api.libs.json.Json

case class VoteVerificationData(
                               id: String,
                               passCode: String
                               )
object VoteVerificationData {
  implicit val voteVerificationDataFormat = Json.format[VoteVerificationData]
}