package utils

import com.mohiva.play.silhouette.api.LoginInfo
import play.api.libs.json.{OFormat, OWrites, Reads}
import reactivemongo.api.bson.{BSONDocumentHandler, BSONHandler, BSONLong, BSONValue, Macros}
import reactivemongo.play.json.compat.json2bson

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
import scala.util.{Success, Try}

object BSONUtils {
  implicit def JsonToBSONDocumentHandler[T](implicit writes: OWrites[T], reads: Reads[T]): BSONDocumentHandler[T] = {
    json2bson.toDocumentHandlerConv(OFormat(reads, writes))
  }

  implicit def OFormatToBSONDocumentHandler[T](implicit oFormat: OFormat[T]): BSONDocumentHandler[T] = {
    json2bson.toDocumentHandlerConv(oFormat)
  }

  implicit object FiniteDurationBsonFormat extends BSONHandler[FiniteDuration] {
    override def readTry(bson: BSONValue): Try[FiniteDuration] =
      bson.asTry[BSONLong].map(secs => new FiniteDuration(secs.value, TimeUnit.SECONDS))

    override def writeTry(duration: FiniteDuration): Try[BSONValue] = Success(
      BSONLong(duration.toSeconds)
    )
  }

  implicit val loginInfoHandler: BSONDocumentHandler[LoginInfo] = Macros.handler[LoginInfo]
}
