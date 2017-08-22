package controllers

import java.util.UUID

import com.google.inject.AbstractModule
import com.mohiva.play.silhouette.api.{ Environment, LoginInfo }
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.test._
import models.User
import net.codingwell.scalaguice.ScalaModule
import org.specs2.mock.Mockito
import org.specs2.specification.Scope
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.concurrent.Execution.Implicits._
import play.api.test.{ FakeRequest, PlaySpecification, WithApplication }
import utils.auth.DefaultEnv
import org.bson.types.ObjectId


/**
 * Test case for the [[controllers.HomeController]] class.
 */
class ElectionControllerSpec extends PlaySpecification with Mockito {
  sequential

  "The `createGuestView` action" should {
    "return 200 if user is unauthorized" in new Context {
      new WithApplication(application) {
        val Some(redirectResult) = route(app, FakeRequest(routes.ElectionController.createGuestView())
          .withAuthenticator[DefaultEnv](LoginInfo("invalid", "invalid"))
        )
        status(redirectResult) must beEqualTo(OK)
      }
    }
    "redirect to login page if user is authorized" in new Context {
      new WithApplication(application) {
        val Some(redirectResult) = route(app, FakeRequest(routes.ElectionController.createGuestView())
          .withAuthenticator[DefaultEnv](identity.loginInfo)
        )
        status(redirectResult) must be equalTo SEE_OTHER
        val redirectURL = redirectLocation(redirectResult).getOrElse("")
        redirectURL must contain(routes.HomeController.indexAuthorized().toString)
        val Some(unauthorizedResult) = route(app, FakeRequest(GET, redirectURL)
        .withAuthenticator[DefaultEnv](identity.loginInfo)
        )
        status(unauthorizedResult) must be equalTo OK
        contentType(unauthorizedResult) must beSome("text/html")
        contentAsString(unauthorizedResult) must contain("Welcome, you are now signed in!")
      }
    }
  }

  "The `createUserView` action" should {
    "redirect to login page if user is unauthorized" in new Context {
      new WithApplication(application) {
        val Some(redirectResult) = route(app, FakeRequest(routes.ElectionController.createUserView())
          .withAuthenticator[DefaultEnv](LoginInfo("invalid", "invalid"))
        )
        status(redirectResult) must be equalTo SEE_OTHER
        val redirectURL = redirectLocation(redirectResult).getOrElse("")
        redirectURL must contain(routes.SignInController.view().toString)
        val Some(unauthorizedResult) = route(app, FakeRequest(GET, redirectURL))
        status(unauthorizedResult) must be equalTo OK
        contentType(unauthorizedResult) must beSome("text/html")
        contentAsString(unauthorizedResult) must contain("Sign In")
      }
    }
    "return 200 if user is authorized" in new Context {
      new WithApplication(application) {
        val Some(result) = route(app, FakeRequest(routes.ElectionController.createUserView())
          .withAuthenticator[DefaultEnv](identity.loginInfo)
        )
        status(result) must beEqualTo(OK)
      }
    }
  }

  "The `viewElection` action" should {
    "return 200 if user is unauthorized" in new Context {
      new WithApplication(application) {
        val Some(redirectResult) = route(app, FakeRequest(routes.ElectionController.viewElection({new ObjectId()}.toString))
          .withAuthenticator[DefaultEnv](LoginInfo("invalid", "invalid"))
        )
        status(redirectResult) must beEqualTo(OK)
      }
    }
    "redirect to login page if user is authorized" in new Context {
      new WithApplication(application) {
        val Some(redirectResult) = route(app, FakeRequest(routes.ElectionController.viewElection({new ObjectId()}.toString))
          .withAuthenticator[DefaultEnv](identity.loginInfo)
        )
        status(redirectResult) must be equalTo SEE_OTHER
        val redirectURL = redirectLocation(redirectResult).getOrElse("")
        redirectURL must contain(routes.HomeController.indexAuthorized().toString)
        val Some(unauthorizedResult) = route(app, FakeRequest(GET, redirectURL)
        .withAuthenticator[DefaultEnv](identity.loginInfo)
        )
        status(unauthorizedResult) must be equalTo OK
        contentType(unauthorizedResult) must beSome("text/html")
        contentAsString(unauthorizedResult) must contain("Welcome, you are now signed in!")
      }
    }
  }

  "The `viewElectionSecured` action" should {
    "redirect to login page if user is unauthorized" in new Context {
      new WithApplication(application) {
        val Some(redirectResult) = route(app, FakeRequest(routes.ElectionController.viewElectionSecured({new ObjectId()}.toString))
          .withAuthenticator[DefaultEnv](LoginInfo("invalid", "invalid"))
        )
        status(redirectResult) must be equalTo SEE_OTHER
        val redirectURL = redirectLocation(redirectResult).getOrElse("")
        redirectURL must contain(routes.SignInController.view().toString)
        val Some(unauthorizedResult) = route(app, FakeRequest(GET, redirectURL))
        status(unauthorizedResult) must be equalTo OK
        contentType(unauthorizedResult) must beSome("text/html")
        contentAsString(unauthorizedResult) must contain("Sign In")
      }
    }
    "redirect to profile page if user is authorized but id is not found" in new Context {
      new WithApplication(application) {
        val Some(result) = route(app, FakeRequest(routes.ElectionController.viewElectionSecured({new ObjectId()}.toString))
          .withAuthenticator[DefaultEnv](identity.loginInfo)
        )
        status(result) must be equalTo SEE_OTHER
        val redirectURL = redirectLocation(result).getOrElse("")
        redirectURL must contain(routes.HomeController.profile().toString)
        val Some(authorizedResult) = route(app, FakeRequest(GET, redirectURL)
        .withAuthenticator[DefaultEnv](identity.loginInfo))
        status(authorizedResult) must be equalTo OK
        contentType(authorizedResult) must beSome("text/html")
        contentAsString(authorizedResult) must contain("Election Details")
      }
    }
    // "return 200 if user is authorized and id is valid" in new Context {
    //   new WithApplication(application) {
    //     val Some(result) = route(app, FakeRequest(routes.ElectionController.viewElectionSecured({new ObjectId()}.toString))
    //       .withAuthenticator[DefaultEnv](identity.loginInfo)
    //     )
    //     status(result) must beEqualTo(OK)
    //   }
    // }
  }

  "The `voteGuest` action" should {
    "redirect to redirectVoting page if id is invalid while user is unauthorized" in new Context {
      new WithApplication(application) {
        val Some(redirectResult) = route(app, FakeRequest(routes.ElectionController.voteGuest({new ObjectId()}.toString))
          .withAuthenticator[DefaultEnv](LoginInfo("invalid", "invalid"))
        )
        status(redirectResult) must be equalTo SEE_OTHER
        val redirectURL = redirectLocation(redirectResult).getOrElse("")
        redirectURL must contain(routes.ElectionController.redirectVoter().toString)
        val Some(unauthorizedResult) = route(app, FakeRequest(GET, redirectURL))
        status(unauthorizedResult) must be equalTo OK
        contentType(unauthorizedResult) must beSome("text/html")
        contentAsString(unauthorizedResult) must contain("Home")
      }
    }
    "redirect to redirectVoting page if user is authorized but id is not found" in new Context {
      new WithApplication(application) {
        val Some(redirectResult) = route(app, FakeRequest(routes.ElectionController.voteGuest({new ObjectId()}.toString))
          .withAuthenticator[DefaultEnv](identity.loginInfo)
        )
        status(redirectResult) must be equalTo SEE_OTHER
        val redirectURL = redirectLocation(redirectResult).getOrElse("")
        redirectURL must contain(routes.ElectionController.redirectVoter().toString)
        val Some(unauthorizedResult) = route(app, FakeRequest(GET, redirectURL))
        status(unauthorizedResult) must be equalTo OK
        contentType(unauthorizedResult) must beSome("text/html")
        contentAsString(unauthorizedResult) must contain("Home")
      }
    }
    // "return 200 if id is valid" in new Context {
    //   new WithApplication(application) {
    //     val Some(result) = route(app, FakeRequest(routes.ElectionController.viewElectionSecured({new ObjectId()}.toString))
    //       .withAuthenticator[DefaultEnv](identity.loginInfo)
    //     )
    //     status(result) must beEqualTo(OK)
    //   }
    // }
  }




  /**
   * The context.
   */
  trait Context extends Scope {

    /**
     * A fake Guice module.
     */
    class FakeModule extends AbstractModule with ScalaModule {
      def configure() = {
        bind[Environment[DefaultEnv]].toInstance(env)
      }
    }

    /**
     * An identity.
     */
    val identity = User(
      userID = UUID.randomUUID(),
      loginInfo = LoginInfo("facebook", "user@facebook.com"),
      firstName = None,
      lastName = None,
      fullName = None,
      email = None,
      avatarURL = None
    )

    /**
     * A Silhouette fake environment.
     */
    implicit val env: Environment[DefaultEnv] = new FakeEnvironment[DefaultEnv](Seq(identity.loginInfo -> identity))

    /**
     * The application.
     */
    lazy val application = new GuiceApplicationBuilder()
      .overrides(new FakeModule)
      .build()
  }
}
