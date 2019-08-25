package formatters.json

import io.swagger.annotations.{ApiModel, ApiModelProperty}
import org.joda.time.DateTime
import play.api.libs.json._

import scala.util.{Failure, Success, Try}


@ApiModel(description = "Totp Token object")
case class Crypto (
                  @ApiModelProperty(value = "crypto", required = true) crypto: String,
                  @ApiModelProperty(value = "otp", required = true) otp: String,
                  @ApiModelProperty(value = "trustedDevice", required = true)trustedDevice: Boolean)

object Crypto {

  implicit val cryptoFormat = Json.format[Crypto]

}
