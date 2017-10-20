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
import models.daos.ElectionDAOImpl


import play.api.Application
import play.api.test.FakeRequest
import play.filters.csrf.CSRF.Token
import play.filters.csrf.{CSRFConfigProvider, CSRFFilter}


import scala.language.postfixOps

import java.util.Date

/**
 * Test case for the [[controllers.HomeController]] class.
 */
class ElectionControllerSpec extends PlaySpecification with Mockito with CSRFTest {
  val mockElectionDAO = new ElectionDAOImpl()
  sequential


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
        contentAsString(authorizedResult) must contain("My Elections")
      }
    }
    "return 200 if user is authorized and id is valid" in new Context {
      new WithApplication(application) {
        val list = mockElectionDAO.userElectionList(Option("test.unit1@gmail.com"))
        if(list.size>0){
          val election = list.head
          val Some(result) = route(app, FakeRequest(routes.ElectionController.viewElectionSecured({election.id}.toString))
            .withAuthenticator[DefaultEnv](identity.loginInfo)
          )
          status(result) must beEqualTo(OK)
        }
      }
    }
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
    "redirect if election is not started " in new Context {
      new WithApplication(application) {
        val list = mockElectionDAO.userElectionList(Option("test.unit1@gmail.com"))
        if(list.size>0){
          val election = list.head
          val Some(result) = route(app, FakeRequest(routes.ElectionController.voteGuest({election.id}.toString))
            .withAuthenticator[DefaultEnv](identity.loginInfo)
          )
          status(result) must be equalTo SEE_OTHER
        }
        for(election <- list){
          mockElectionDAO.delete(election.id)
        }
      }
    }
  }

  "The `create` action" should {
  "return 200 if user is authorized and request has CSRF" in new Context {
    new WithApplication(application) {
      val Some(redirectResult) = route(app, addToken(FakeRequest(routes.ElectionController.create()))
        .withAuthenticator[DefaultEnv](identity.loginInfo).withFormUrlEncodedBody(
          "name" -> "FooBar", "description" -> "blabala", "creatorName"-> "Thuva",
          "creatorEmail" -> "test.unit@gmail.com",
          "start" -> "03/08/2017" , "end" -> "04/08/2017",
          "realtimeResult" -> "false",  "votingAlgo" -> "SMC",
          "candidates" -> "A,B,C", "ballotVisibility" -> false.toString,
          "voterListVisibility" -> false.toString, "isInvite" -> false.toString,
          "noVacancies" -> "4"
        ))
      status(redirectResult) must beEqualTo(OK)
      val list = mockElectionDAO.userElectionList(Option("test.unit@gmail.com"))
      for(election <- list){
        mockElectionDAO.delete(election.id)
      }
    }
  }
  "redirect to login page if user is unauthorized" in new Context {
    new WithApplication(application) {
      val Some(redirectResult) = route(app, addToken(FakeRequest(routes.ElectionController.create()))
         .withAuthenticator[DefaultEnv](LoginInfo("invalid", "invalid")).withFormUrlEncodedBody(
          "name" -> "FooBar", "description" -> "blabala", "creatorName"-> "Thuva",
          "creatorEmail" -> "test.unit@gmail.com",
          "start" -> "03/08/2017" , "end" -> "04/08/2017",
          "realtimeResult" -> "false",  "votingAlgo" -> "SMC",
          "candidates" -> "A,B,C", "ballotVisibility" -> false.toString,
          "voterListVisibility" -> false.toString, "isInvite" -> false.toString,
          "noVacancies" -> "4"
        ))
      status(redirectResult)  must be equalTo SEE_OTHER
      val redirectURL = redirectLocation(redirectResult).getOrElse("")
      redirectURL must contain(routes.SignInController.view().toString)
      val Some(unauthorizedResult) = route(app, FakeRequest(GET, redirectURL))
      status(unauthorizedResult) must be equalTo OK
      contentType(unauthorizedResult) must beSome("text/html")
      contentAsString(unauthorizedResult) must contain("Sign In")
    }
  }
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

trait CSRFTest {
  def addToken[T](fakeRequest: FakeRequest[T])(implicit app: Application) = {
    val csrfConfig     = app.injector.instanceOf[CSRFConfigProvider].get
    val csrfFilter     = app.injector.instanceOf[CSRFFilter]
    val token          = csrfFilter.tokenProvider.generateToken

    fakeRequest.copyFakeRequest(tags = fakeRequest.tags ++ Map(
      Token.NameRequestTag  -> csrfConfig.tokenName,
      Token.RequestTag      -> token
    )).withHeaders((csrfConfig.headerName, token))
  }
}
