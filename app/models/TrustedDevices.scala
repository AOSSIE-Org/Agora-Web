package models

import com.mohiva.play.silhouette.api.LoginInfo
import org.joda.time.DateTime
import play.api.libs.json._
import reactivemongo.api.bson.BSONDocumentHandler

case class TrustedDevices(userLoginInfo: LoginInfo, trustedDevice: String, expiry: DateTime)

object TrustedDevices {
  implicit val loginInfoFormat: OFormat[LoginInfo] = Json.format[LoginInfo]

  implicit val jodaDateReads: Reads[DateTime] = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSZZ")
  implicit val jodaDateWrites: Writes[DateTime] = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSZZ")

  implicit val trustedDevicesFormat: OFormat[TrustedDevices] = Json.format[TrustedDevices]

  implicit val handler: BSONDocumentHandler[TrustedDevices] = utils.BSONUtils.OFormatToBSONDocumentHandler
}