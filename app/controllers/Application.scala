package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import anorm._

import models._
import views._

object Application extends Controller {

  //Authentication
  val loginForm = Form(
    tuple(
      "username" ->text,
      "passwd" -> text
      ) verifying ("Invalid username or password", result => result match {
      case (username, passwd) => User.authenticate(username, passwd).isDefined
    }
    )
      )

  //create signup form
  val signupForm = Form(
    mapping(
      "username" -> nonEmptyText,
      "fullname" -> text,
      "passwd" -> text,
      "email" -> text
      )(User.apply)(User.unapply) 
    )

  //authenticate action
  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.login(formWithErrors)),
      user => Redirect(routes.Twitter.index).withSession("username" -> user._1)
      )
  }

  //Login action
  def login = Action { request =>
    request.session.get("username").map { user =>
      Ok(html.login(loginForm))  
    }.getOrElse {
      Redirect(routes.Twitter.index)
    }

    
  }

  //Logout and clean the session
  def logout = Action {
    Redirect(routes.Application.login).withNewSession.flashing(
      "success" -> "You've been logged out!"
      )
  }

  //index page
  def index = Action { request =>
    request.session.get("username").map { user =>
      Ok(views.html.index)  
    }.getOrElse {
      Redirect(routes.Twitter.index) 
    }
  }

  //signup action
  def signup = Action { request =>
    request.session.get("username").map {user =>
    Ok(html.signup(signupForm))  
    }.getOrElse {
      Redirect(routes.Twitter.index)
    }
    
  }
  
  //Submit action
  def submit = Action { request =>
    signupForm.bindFromRequest.fold(
      //if(User.findByUsername(signupForm.field("username").value) != null) {
      //signupForm.reject("username" , "user already exist")
    //}
      formWithErrors => BadRequest(html.signup(formWithErrors)),
      user => {
        if(User.findByUsername(signupForm.field("username").value) != null) {
          if(User.inset(signupForm.field("fullname").value, signupForm.field("username").value, signupForm.field("passwd").value, signupForm.field("email").value) != 0) {
            Ok(views.html.signupdone(signupForm.field("username").value))
          } else {
            BadRequest(html.signup(signupForm))
          }
          } else {
            BadRequest(html.signup(signupForm))
          }
      }
      )
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