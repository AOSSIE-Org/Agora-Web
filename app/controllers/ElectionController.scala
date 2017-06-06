package controllers

import javax.inject._
import play.api._
import play.api.mvc._

import models.Election
import play.api.data._
import play.api.data.Forms._

import play.api.i18n.Messages.Implicits._
import play.api.Play.current

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */

@Singleton
class ElectionController @Inject() extends Controller {

  /**
    * Create an Action to render an HTML page.
    *
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */



  def electionForm = Form(mapping("Name"  -> nonEmptyText, "Description" -> nonEmptyText, "Creator name" -> nonEmptyText,
    "Creator email" -> email, "Starting Date" -> nonEmptyText, "Ending Date" -> nonEmptyText, "Realtime Result" -> boolean,
    "Voting Algo" -> nonEmptyText, "Canditates" -> list(text), "Voting Preference" -> nonEmptyText,
    "Invite Voters" -> boolean)(Election.apply)(Election.unapply))

  def create = Action { implicit request =>
    electionForm.bindFromRequest().fold(
      formWithErrors => BadRequest(views.html.addElection(formWithErrors)),
      {

        election => {
          Election.create(election)
          Ok(views.html.home())
        }
      })
  }


  def createview = Action { implicit request =>
    Ok(views.html.addElection(electionForm))
  }



}
