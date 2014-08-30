package controllers

import play.api._
import play.api.mvc._
import play.api.data.Forms._
import play.api.data._

import models._
import views._
/** Uncomment the following lines as needed **/
/**
import play.api.Play.current
import play.api.libs._
import play.api.libs.iteratee._
import play.api.libs.concurrent._
import java.util.concurrent._
import scala.concurrent.stm._
import akka.util.duration._
import play.api.cache._
import play.api.libs.json._
**/

object FriendSearch extends Controller with Secured{
	//search text
	case class SearchKey(fsearch: String)

	val getSearchInput = Form(
		mapping(
			"fsearch" -> text
		)(SearchKey.apply)(SearchKey.unapply)
	)
	
	//def search index
	val Home = Redirect(routes.FriendSearch.search(0, ""))

	//Home
	def index = Action {Home}

	//search friend function
	def friendsearch = Action { request =>
		Ok(html.friendsearch())
	}

	//action search
	def dosearch = Action { implicit request =>
		getSearchInput.bindFromRequest.fold(
			formWithErrors => BadRequest("Oh noes, invalid submission!"),
			value => Redirect(routes.FriendSearch.search(0, value.fsearch))
			)
		
	}

	//search
	def search(page: Int, keysearch: String) = Action { request =>
		request.session.get("username").map { user =>
			Ok(views.html.searchresult(User.page(user, page, 10, "username", "asc", keysearch), keysearch))
		}.getOrElse {
			Redirect(routes.Twitter.index)
		}
		
	}

	//add Follow from search page
	def addFollow(id: String, filter: String, page: Int) = Action { request =>
		request.session.get("username").map { user =>
			User_Follow.add(user, id)
			Redirect(routes.FriendSearch.search(page, filter)).withNewSession.flashing(
			"success" -> "This user has been followed"
			)
	}.getOrElse {
		Redirect(routes.Twitter.index)
	}
		}
		

	//def
	def frisearch = Action { implicit request =>
		getSearchInput.bindFromRequest.fold(
			errors => BadRequest(html.friendsearch()),
			user => Redirect(routes.FriendSearch.search(0, user.fsearch))
			)
		
	}
}