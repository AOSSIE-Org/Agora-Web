package utils.auth

import com.mohiva.play.silhouette.api.{Env, Environment, Silhouette}
import com.mohiva.play.silhouette.api.actions.{SecuredAction, SecuredActionBuilder, SecuredRequestHandlerBuilder, UnsecuredAction, UnsecuredActionBuilder, UnsecuredRequestHandlerBuilder, UserAwareAction, UserAwareActionBuilder, UserAwareRequestHandlerBuilder}
import javax.inject.Inject
import play.api.mvc.AnyContent
/**
 * The Silhouette stack.
 *
 * Inject an instance of this trait into your controller to provide all the Silhouette actions.
 *
 * @tparam E The type of the environment.
 */
trait CustomSilhouette[E <: Env] extends Silhouette[E]{

  /**
   * The Silhouette environment.
   */
  override val env: CustomEnvironment[E]
}

class CustomSilhouetteProvider [E <: Env] @Inject() (
                                                      val env: CustomEnvironment[E],
                                                      val securedAction: SecuredAction,
                                                      val unsecuredAction: UnsecuredAction,
                                                      val userAwareAction: UserAwareAction
                                                    ) extends CustomSilhouette[E]
