package jobs

import javax.inject.Inject

import akka.actor._
import com.mohiva.play.silhouette.api.util.Clock
import jobs.AuthTokenCleaner.Clean
import models.services.AuthTokenService
import utils.Logger

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * A job which cleanup invalid auth tokens.
 *
 * @param service The auth token service implementation.
 * @param clock The clock implementation.
 */
class AuthTokenCleaner @Inject()(
  service: AuthTokenService,
  clock: Clock
) extends Actor with Logger {

  /**
   * Process the received messages.
   */
  def receive: Receive = {
    case Clean =>
      val start = clock.now.getMillis
      logger.info(
        """
          |=================================
          |Start to cleanup auth tokens
          |=================================
        """.stripMargin)
      service.clean.map { deleted =>
        val seconds = (clock.now.getMillis - start) / 1000
        logger.info(
          s"""
             |Total of ${deleted.length} auth tokens(s) were deleted in $seconds seconds
             |=================================
             |=================================
           """.stripMargin)
      }.recover {
        case e =>
          logger.error(
            """
              |Couldn't cleanup auth tokens because of unexpected error
              |=================================
            """.stripMargin, e)
      }
  }
}

/**
 * The companion object.
 */
object AuthTokenCleaner {
  case object Clean
}
