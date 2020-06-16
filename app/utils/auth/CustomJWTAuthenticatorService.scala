package utils.auth

import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import com.mohiva.play.silhouette.api.Authenticator.Implicits._
import com.mohiva.play.silhouette.api.crypto.AuthenticatorEncoder
import com.mohiva.play.silhouette.api.exceptions._
import com.mohiva.play.silhouette.api.repositories.AuthenticatorRepository
import com.mohiva.play.silhouette.api.services.AuthenticatorService._
import com.mohiva.play.silhouette.api.services.AuthenticatorResult
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.api.{Logger, LoginInfo}
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator._
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticatorService._
import play.api.mvc.{RequestHeader, Result}
import com.mohiva.play.silhouette.impl.authenticators.{JWTAuthenticator, JWTAuthenticatorService, JWTAuthenticatorSettings}
import repository.RefreshTokenAuthenticatorRepository

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

class CustomJWTAuthenticatorService(
                                     authTokenSettings: JWTAuthenticatorSettings,
                                     refreshTokenSettings: RefreshTokenAuthenticatorSettings,
                                     authTokenRepository: Option[AuthenticatorRepository[JWTAuthenticator]],
                                     refreshTokenRepository: Option[RefreshTokenAuthenticatorRepository[JWTAuthenticator]],
                                     authenticatorEncoder: AuthenticatorEncoder,
                                     idGenerator: IDGenerator,
                                     clock: Clock
                                   )
                                   (implicit override val executionContext: ExecutionContext)
  extends CustomAuthenticatorService[JWTAuthenticator]
    with Logger {

  // Auth token authenticator methods

  /**
   * Creates a new authenticator for the specified login info.
   *
   * @param loginInfo The login info for which the authenticator should be created.
   * @param request   The request header.
   * @return An authenticator.
   */
  override def create(loginInfo: LoginInfo)(implicit request: RequestHeader): Future[JWTAuthenticator] = {
    idGenerator.generate.map { id =>
      val now = clock.now
      JWTAuthenticator(
        id = id,
        loginInfo = loginInfo,
        lastUsedDateTime = now,
        expirationDateTime = now + authTokenSettings.authenticatorExpiry,
        idleTimeout = authTokenSettings.authenticatorIdleTimeout
      )
    }.recover {
      case e => throw new AuthenticatorCreationException(CreateError.format(ID, loginInfo), e)
    }
  }

  /**
   * Retrieves the authenticator from request.
   *
   * If a backing store is defined, then the authenticator will be validated against it.
   *
   * @param request The request to retrieve the authenticator from.
   * @tparam B The type of the request body.
   * @return Some authenticator or None if no authenticator could be found in request.
   */
  override def retrieve[B](implicit request: ExtractableRequest[B]): Future[Option[JWTAuthenticator]] = {
    Future.fromTry(Try(request.extractString(authTokenSettings.fieldName, authTokenSettings.requestParts))).flatMap {
      case Some(token) => unserialize(token, authenticatorEncoder, authTokenSettings) match {
        case Success(authenticator) => authTokenRepository.fold(Future.successful(Option(authenticator)))(_.find(authenticator.id))
        case Failure(e) =>
          logger.info(e.getMessage, e)
          Future.successful(None)
      }
      case None => Future.successful(None)
    }.recover {
      case e => throw new AuthenticatorRetrievalException(RetrieveError.format(ID), e)
    }
  }

  /**
   * Creates a new JWT for the given authenticator and return it. If a backing store is defined, then the
   * authenticator will be stored in it.
   *
   * @param authenticator The authenticator instance.
   * @param request       The request header.
   * @return The serialized authenticator value.
   */
  override def init(authenticator: JWTAuthenticator)(implicit request: RequestHeader): Future[String] = {
    authTokenRepository.fold(Future.successful(authenticator))(_.add(authenticator)).map { a =>
      serialize(a, authenticatorEncoder, authTokenSettings)
    }.recover {
      case e => throw new AuthenticatorInitializationException(InitError.format(ID, authenticator), e)
    }
  }

  /**
   * Adds a header with the token as value to the result.
   *
   * @param token  The token to embed.
   * @param result The result to manipulate.
   * @return The manipulated result.
   */
  override def embed(token: String, result: Result)(implicit request: RequestHeader): Future[AuthenticatorResult] = {
    Future.successful(AuthenticatorResult(result.withHeaders(authTokenSettings.fieldName -> token)))
  }

  /**
   * Adds a header with the token as value to the request.
   *
   * @param token   The token to embed.
   * @param request The request header.
   * @return The manipulated request header.
   */
  override def embed(token: String, request: RequestHeader): RequestHeader = {
    val additional = Seq(authTokenSettings.fieldName -> token)
    request.withHeaders(request.headers.replace(additional: _*))
  }

  /**
   * @inheritdoc
   * @param authenticator The authenticator to touch.
   * @return The touched authenticator on the left or the untouched authenticator on the right.
   */
  override def touch(authenticator: JWTAuthenticator): Either[JWTAuthenticator, JWTAuthenticator] = {
    if (authenticator.idleTimeout.isDefined) {
      Left(authenticator.copy(lastUsedDateTime = clock.now))
    } else {
      Right(authenticator)
    }
  }

  /**
   * Updates the authenticator and embeds a new token in the result.
   *
   * To prevent the creation of a new token on every request, disable the idle timeout setting and this
   * method will not be executed.
   *
   * @param authenticator The authenticator to update.
   * @param result        The result to manipulate.
   * @param request       The request header.
   * @return The original or a manipulated result.
   */
  override def update(authenticator: JWTAuthenticator, result: Result)(
    implicit
    request: RequestHeader): Future[AuthenticatorResult] = {

    authTokenRepository.fold(Future.successful(authenticator))(_.update(authenticator)).map { a =>
      AuthenticatorResult(result.withHeaders(authTokenSettings.fieldName -> serialize(a, authenticatorEncoder, authTokenSettings)))
    }.recover {
      case e => throw new AuthenticatorUpdateException(UpdateError.format(ID, authenticator), e)
    }
  }

  /**
   * Renews an authenticator.
   *
   * After that it isn't possible to use a JWT which was bound to this authenticator. This method
   * doesn't embed the the authenticator into the result. This must be done manually if needed
   * or use the other renew method otherwise.
   *
   * @param authenticator The authenticator to renew.
   * @param request       The request header.
   * @return The serialized expression of the authenticator.
   */
  override def renew(authenticator: JWTAuthenticator)(implicit request: RequestHeader): Future[String] = {
    authTokenRepository.fold(Future.successful(()))(_.remove(authenticator.id)).flatMap { _ =>
      create(authenticator.loginInfo).map(_.copy(customClaims = authenticator.customClaims)).flatMap(init)
    }.recover {
      case e => throw new AuthenticatorRenewalException(RenewError.format(ID, authenticator), e)
    }
  }

  /**
   * Renews an authenticator and replaces the JWT header with a new one.
   *
   * If a backing store is defined, the old authenticator will be revoked. After that, it isn't
   * possible to use a JWT which was bound to this authenticator.
   *
   * @param authenticator The authenticator to update.
   * @param result        The result to manipulate.
   * @param request       The request header.
   * @return The original or a manipulated result.
   */
  override def renew(authenticator: JWTAuthenticator, result: Result)(
    implicit
    request: RequestHeader): Future[AuthenticatorResult] = {

    renew(authenticator).flatMap(v => embed(v, result)).recover {
      case e => throw new AuthenticatorRenewalException(RenewError.format(ID, authenticator), e)
    }
  }

  /**
   * Removes the authenticator from backing store.
   *
   * @param result  The result to manipulate.
   * @param request The request header.
   * @return The manipulated result.
   */
  override def discard(authenticator: JWTAuthenticator, result: Result)(
    implicit
    request: RequestHeader): Future[AuthenticatorResult] = {

    authTokenRepository.fold(Future.successful(()))(_.remove(authenticator.id)).map { _ =>
      AuthenticatorResult(result)
    }.recover {
      case e => throw new AuthenticatorDiscardingException(DiscardError.format(ID, authenticator), e)
    }
  }


  // RefreshToken methods.


  /**
   * Creates a new authenticator for the specified login info.
   *
   * @param loginInfo The login info for which the authenticator should be created.
   * @param request   The request header.
   * @return An authenticator.
   */
  override def createRefreshToken(loginInfo: LoginInfo)(implicit request: RequestHeader): Future[JWTAuthenticator] = {
    idGenerator.generate.map { id =>
      val now = clock.now
      JWTAuthenticator(
        id = id,
        loginInfo = loginInfo,
        lastUsedDateTime = now,
        expirationDateTime = now + refreshTokenSettings.authenticatorExpiry,
        idleTimeout = refreshTokenSettings.authenticatorIdleTimeout
      )
    }.recover {
      case e => throw new AuthenticatorCreationException(CreateError.format(ID, loginInfo), e)
    }
  }

  /**
   * Retrieves the authenticator from request.
   *
   * @param request The request to retrieve the authenticator from.
   * @tparam B The type of the request body.
   * @return Some authenticator or None if no authenticator could be found in request.
   */
  override def retrieveRefreshToken[B](implicit request: ExtractableRequest[B]): Future[Option[JWTAuthenticator]] = {
    Future.fromTry(Try(request.extractString(refreshTokenSettings.fieldName, refreshTokenSettings.requestParts))).flatMap {
      case Some(token) => unserialize(token, authenticatorEncoder, refreshTokenSettings.toJWTAuthenticatorSettings) match {
        case Success(authenticator) => refreshTokenRepository.fold(Future.successful(Option(authenticator)))(_.find(authenticator.id))
        case Failure(e) =>
          logger.info(e.getMessage, e)
          Future.successful(None)
      }
      case None => Future.successful(None)
    }.recover {
      case e => throw new AuthenticatorRetrievalException(RetrieveError.format(ID), e)
    }
  }

  /**
   * Initializes an authenticator and instead of embedding into the the request or result, it returns
   * the serialized value.
   *
   * @param authenticator The authenticator instance.
   * @param request       The request header.
   * @return The serialized authenticator value.
   */
  override def initRefreshToken(authenticator: JWTAuthenticator)(implicit request: RequestHeader): Future[String] = {
    refreshTokenRepository.fold(Future.successful(authenticator))(_.add(authenticator)).map { a =>

      serialize(a, authenticatorEncoder, refreshTokenSettings.toJWTAuthenticatorSettings)
    }.recover {
      case e => throw new AuthenticatorInitializationException(InitError.format(ID, authenticator), e)
    }
  }

  /**
   * Manipulates the response and removes authenticator specific artifacts before sending it to the client.
   *
   * @param authenticator The authenticator instance.
   * @param request       The request header.
   * @return The manipulated result.
   */
  override def discardRefreshToken(authenticator: JWTAuthenticator)(implicit request: RequestHeader): Future[Unit] = {

    refreshTokenRepository.fold(Future.successful(()))(_.remove(authenticator.id)).flatMap { _ => Future.successful()
    }.recover {
      case e => throw new AuthenticatorDiscardingException(DiscardError.format(ID, authenticator), e)
    }
  }

  /**
   * Retrieves the authenticator from request.
   *
   * @param request The request to retrieve the authenticator from.
   * @tparam B The type of the request body.
   * @return Some token or None if no authenticator could be found in request.
   */
  override def retrieveRefreshTokenFromRequest[B](implicit request: ExtractableRequest[B]): Future[Option[String]] = Future.fromTry(Try(request.extractString(refreshTokenSettings.fieldName, refreshTokenSettings.requestParts)))
}

/**
 * The settings for the JWT authenticator.
 *
 * @param fieldName                The name of the field in which the token will be transferred in any part
 *                                 of the request.
 * @param requestParts             Some request parts from which a value can be extracted or None to extract
 *                                 values from any part of the request.
 * @param issuerClaim              The issuer claim identifies the principal that issued the JWT.
 * @param authenticatorIdleTimeout The duration an authenticator can be idle before it timed out.
 * @param authenticatorExpiry      The duration an authenticator expires after it was created.
 * @param sharedSecret             The shared secret to sign the JWT.
 */
case class RefreshTokenAuthenticatorSettings(
                                              fieldName: String = "X-Refresh-Token",
                                              requestParts: Option[Seq[RequestPart.Value]] = Some(Seq(RequestPart.Headers)),
                                              issuerClaim: String = "play-silhouette",
                                              authenticatorIdleTimeout: Option[FiniteDuration] = None,
                                              authenticatorExpiry: FiniteDuration = 720 hours, //30 days
                                              sharedSecret: String) {
  def toJWTAuthenticatorSettings: JWTAuthenticatorSettings = JWTAuthenticatorSettings(fieldName, requestParts, issuerClaim, authenticatorIdleTimeout, authenticatorExpiry, sharedSecret)
}