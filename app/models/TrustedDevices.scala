package models

import com.mohiva.play.silhouette.api.LoginInfo
import org.joda.time.DateTime
import play.api.libs.json._

case class TrustedDevices(userLoginInfo: LoginInfo, trustedDevice: String, expiry: DateTime)

object TrustedDevices {
  implicit val loginInfoFormat = Json.format[LoginInfo]

  implicit val jodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSZZ")
  implicit val jodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSZZ")

  implicit val trustedDevicesFormat = Json.format[TrustedDevices]
}