package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import models._
import views._

object Application extends Controller {

  //Authentication
  val loginForm = Form(
    tuple(
      "username" ->text,
      "passwd" -> text
      ) verifying ("Invalid username or password", result => result match) {
      case (username, passwd) => User.authenticate(username, passwd).isDefined
    }
    )

  //create signup form
  val signupForm = Form(
    tuple(
      "username" -> text,
      "fullname" -> text,
      "passwd" -> text,
      "email" -> text
      ) verifying("Can't insert data", result => result match{
        case(username, fullname, passwd, email) => User.insert(fullname, username, passwd, email).isDefined
        })
    )

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  

}

//Authentication

trait Secured {
	//Try to connect user account
	private def username(request: RequestHeader) = request.session.get("username")

	//Redirect to login if user is not authorized
	private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Application.login)

	//Action for authenticated users

	def IsAuthenticated(f: => String => Request[AnyContent] => Result) = Security.Authenticated(username, onUnauthorized) { user =>
		Action(request => f(user)(request))
	}
}