package controllers

import javax.inject.{Inject, Singleton}
import com.mohiva.play.silhouette.api.Silhouette
import io.swagger.annotations.{Api, ApiOperation}
import play.api.libs.json.Json
import play.api.mvc._
import utils.auth.{CustomSilhouette, DefaultEnv}

import scala.concurrent.Future

@Singleton
class ApplicationController @Inject()(components: ControllerComponents,
                                      silhouette: CustomSilhouette[DefaultEnv]) extends AbstractController(components) {

  /**
    * Create an Action to render an HTML page with a welcome message.
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
  @ApiOperation(value = "", hidden = true)
  def index  = Action { implicit request =>
    Redirect(
      url = "/assets/lib/swagger-ui/index.html",
      queryString = Map("url" -> Seq("http://" + request.host + "/swagger.json"))
    )
  }
}