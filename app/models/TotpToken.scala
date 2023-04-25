package models

import com.mohiva.play.silhouette.api.LoginInfo
import org.joda.time.DateTime
import play.api.libs.json._
import reactivemongo.api.bson.{BSONDocumentHandler, Macros}

/**
 * A token for two factor authentication
 *
 * @param crypto Shared secret between client and server
 * @param expiry The date-time the token expires.
 * @param attempts Number of wrong attempts
 */
case class TotpToken(crypto: String, userLoginInfo: LoginInfo, expiry: DateTime, attempts: Int)

object TotpToken {
  implicit val loginInfoFormat = Json.format[LoginInfo]

  implicit val jodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSZZ")
  implicit val jodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSZZ")

  implicit val totpTokenFormat = Json.format[TotpToken]

  implicit val handler: BSONDocumentHandler[TotpToken] = utils.BSONUtils.OFormatToBSONDocumentHandler
}