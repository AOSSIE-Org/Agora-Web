package controllers

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import io.swagger.annotations.{Api, ApiOperation}
import play.api.libs.json.Json
import play.api.mvc._
import utils.auth.DefaultEnv

import scala.concurrent.Future

@Api(value = "Example data")
@Singleton
class ApplicationController @Inject()(components: ControllerComponents,
                                      silhouette: Silhouette[DefaultEnv]) extends AbstractController(components) {

  /**
    * Create an Action to render an HTML page with a welcome message.
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
  @ApiOperation(value = "", hidden = true)
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }
}