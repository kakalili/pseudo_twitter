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

object Follow extends Controller with Secured {
	//This result redirect to application home
	val Home = Redirect(routes.Follow.getfollow(0, 0))

	//default path
	def index = Action { Home }

	//get follow
	def getfollow(page: Int, tp: Int) = Action { request =>
		request.session.get("username").map{ user =>
			val following_number: Long = User_Follow.page(user, 0, page, 10).total
			val followed_number: Long = User_Follow.page(user, 1, page, 10).total
			Ok(views.html.follow(
			User_Follow.page(user, tp, page, 10), 
			tp, following_number, followed_number, user
		))
		}.getOrElse {
			 Redirect(routes.Twitter.index)
		}
		
	} //end getfollow

	//unfollow page
	def unfollow(id: Int, page: Int) = Action { request =>
		User_Follow.delete(id)
		Redirect(routes.Follow.getfollow(page, 0)).withNewSession.flashing(
			"success" -> "This user has been unfollowed"
			)
	} //

	//following
	def following(des: String, page: Int) = Action { request =>
		request.session.get("username").map { user =>
			User_Follow.add(user, des)
				
		}
		Redirect(routes.Follow.getfollow(page, 1)).withNewSession.flashing(
			"success" -> "This user has been followed"
			)
	}


}