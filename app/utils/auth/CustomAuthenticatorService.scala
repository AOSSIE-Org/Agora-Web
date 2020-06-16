package utils.auth

import com.mohiva.play.silhouette.api.services.{AuthenticatorResult, AuthenticatorService}
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import com.mohiva.play.silhouette.api.util.{ExecutionContextProvider, ExtractableRequest}
import com.mohiva.play.silhouette.api.{Authenticator, LoginInfo}
import play.api.http.HttpEntity
import play.api.mvc._

import scala.concurrent.Future

trait CustomAuthenticatorService[T <: Authenticator] extends AuthenticatorService[T] {
  /**
   * Creates a new authenticator for the specified login info.
   *
   * @param loginInfo The login info for which the authenticator should be created.
   * @param request   The request header.
   * @return An authenticator.
   */
  def createRefreshToken(loginInfo: LoginInfo)(implicit request: RequestHeader): Future[T]

  /**
   * Retrieves the authenticator from request.
   *
   * @param request The request to retrieve the authenticator from.
   * @tparam B The type of the request body.
   * @return Some authenticator or None if no authenticator could be found in request.
   */
  def retrieveRefreshToken[B](implicit request: ExtractableRequest[B]): Future[Option[T]]

  /**
   * Initializes an authenticator and instead of embedding into the the request or result, it returns
   * the serialized value.
   *
   * @param authenticator The authenticator instance.
   * @param request       The request header.
   * @return The serialized authenticator value.
   */
  def initRefreshToken(authenticator: T)(implicit request: RequestHeader): Future[T#Value]

  /**
   * Manipulates the response and removes authenticator specific artifacts before sending it to the client.
   *
   * @param authenticator The authenticator instance.
   * @param request       The request header.
   * @return The manipulated result.
   */
  def discardRefreshToken(authenticator: T)(implicit request: RequestHeader): Future[Unit]

  /**
   * Retrieves the authenticator from request.
   *
   * @param request The request to retrieve the authenticator from.
   * @tparam B The type of the request body.
   * @return Some token or None if no authenticator could be found in request.
   */
  def retrieveRefreshTokenFromRequest[B](implicit request: ExtractableRequest[B]): Future[Option[T#Value]]
}

