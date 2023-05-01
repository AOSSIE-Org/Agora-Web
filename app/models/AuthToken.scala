package models

import com.mohiva.play.silhouette.api.LoginInfo
import org.joda.time.DateTime
import play.api.libs.json._
import reactivemongo.api.bson.BSONDocumentHandler

/**
 * A token to authenticate a user against an endpoint for a short time period.
 *
 * @param tokenId The unique token ID.
 * @param userLoginInfo The unique ID of the user the token is associated with.
 * @param expiry The date-time the token expires.
 */
case class AuthToken(tokenId: String, userLoginInfo: LoginInfo, expiry: DateTime)

object AuthToken {
  implicit val loginInfoFormat: OFormat[LoginInfo] = Json.format[LoginInfo]

  implicit val jodaDateReads: Reads[DateTime] = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSZZ")
  implicit val jodaDateWrites: Writes[DateTime] = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSZZ")

  implicit val authTokenFormat: OFormat[AuthToken] = Json.format[AuthToken]

  implicit val handler: BSONDocumentHandler[AuthToken] = utils.BSONUtils.OFormatToBSONDocumentHandler(authTokenFormat)
}