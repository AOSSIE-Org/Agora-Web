package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import countvotes._

import models.Customer
import play.api.data._
import play.api.data.Forms._

import play.api.i18n.Messages.Implicits._
import play.api.Play.current

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject() extends Controller {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */

  def customerForm = Form(mapping("Customer Name" -> nonEmptyText,
    "Credit Limit" -> number)(Customer.apply)(Customer.unapply))

  def createCustomer = Action { implicit request =>
    customerForm.bindFromRequest().fold(
      formWithErrors => BadRequest(views.html.index(formWithErrors)),
      customer => Ok(s"Customer ${customer.name} created successfully"))
  }


  def index = Action { implicit request =>
    Ok(views.html.index(customerForm))
  }

  def login = Action {implicit request =>
  Ok(views.html.login())
  }

  def vote = Action { implicit request =>
  Ok (views.html.vote())
  }

  def addelection = Action { implicit request =>
  Ok (views.html.addElection())

  }

}
