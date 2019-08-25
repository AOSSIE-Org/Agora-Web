package formatters.json

import io.swagger.annotations.{ApiModel, ApiModelProperty}

import com.mohiva.play.silhouette.api.util.Credentials
import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, _}
import scala.util.{Failure, Success, Try}

case class CredentialFormat (
                  @ApiModelProperty(value = "identifier", required = true) identifier: String,
                  @ApiModelProperty(value = "password", required = true) password: String,
                  @ApiModelProperty(value = "trustedDevice", required = false)trustedDevice: Option[String])

object CredentialFormat {

  implicit val restFormat = Json.format[CredentialFormat]
  
  implicit object CredentialFormatWrites extends OWrites[CredentialFormat] {
    def writes(credentialFormat: CredentialFormat): JsObject = {
      val json = Json.obj(
        "identifier" -> credentialFormat.identifier,
        "password" -> credentialFormat.password,
        "trustedDevice" -> credentialFormat.trustedDevice
      )
      json
    }
  }
}
