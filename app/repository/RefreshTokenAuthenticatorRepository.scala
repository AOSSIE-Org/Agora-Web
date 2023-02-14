package repository

import com.mohiva.play.silhouette.api.StorableAuthenticator
import com.mohiva.play.silhouette.api.repositories.AuthenticatorRepository

trait RefreshTokenAuthenticatorRepository[T <: StorableAuthenticator] extends AuthenticatorRepository[T]
