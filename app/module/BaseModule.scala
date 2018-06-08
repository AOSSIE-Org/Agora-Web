package module

import com.google.inject.AbstractModule
import dao.{AuthTokenDAO, AuthTokenDAOImpl, ElectionDAOImpl}
import net.codingwell.scalaguice.ScalaModule
import service.{AuthTokenService, AuthTokenServiceImpl, ElectionService}

/**
 * The base Guice module.
 */
class BaseModule extends AbstractModule with ScalaModule {

  /**
   * Configures the module.
   */
  def configure(): Unit = {
    bind[AuthTokenDAO].to[AuthTokenDAOImpl]
    bind[AuthTokenService].to[AuthTokenServiceImpl]
    bind[ElectionService].to[ElectionDAOImpl]
  }
}
