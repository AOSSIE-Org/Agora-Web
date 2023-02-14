package utils.auth

import com.mohiva.play.silhouette.api.services.{AuthenticatorService, IdentityService}
import com.mohiva.play.silhouette.api.{Env, Environment, EventBus, RequestProvider}

import scala.concurrent.ExecutionContext

trait CustomEnvironment[E <: Env] extends Environment[E] {
  /**
   * Gets the authenticator service implementation.
   *
   * @return The authenticator service implementation.
   */
  override def authenticatorService: CustomAuthenticatorService[E#A]
}

object CustomEnvironment {
  def apply[E <: Env](
                       identityServiceImpl: IdentityService[E#I],
                       authenticatorServiceImpl: CustomAuthenticatorService[E#A],
                       requestProvidersImpl: Seq[RequestProvider],
                       eventBusImpl: EventBus)(implicit ec: ExecutionContext) = new CustomEnvironment[E] {
    val identityService = identityServiceImpl
    val authenticatorService = authenticatorServiceImpl
    val requestProviders = requestProvidersImpl
    val eventBus = eventBusImpl
    val executionContext = ec
  }
}