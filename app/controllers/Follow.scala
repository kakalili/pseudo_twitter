package controllers

import play.api._
import play.api.mvc._
import play.api.data.Forms._
import play.api.data._
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
	val Home = Redirect(routes.Application.getfollow(0, 0))

	//default path
	def index = Action { Home }

	//get follow
	def getfollow(page: Int, tp: Int) = Action { username =>
		val following_b = User_Follow.page(username, 0, page, 10)
		val followed_b = User_Follow.page(username, 1, page, 10)
		val following_number: Int = following_b.getTotalRowCount
		val followed_number: Int = followed_b.getTotalRowCount

		Ok(views.html.follow(
			User_Follow.page(username, tp, page, 10), 
			tp, following_number, followed_number, username
		))
	} //end getfollow

	//unfollow page
	def unfollow(id: Int, page: Int) = Action { username =>
		User_Follow.delete(id)
		Redirect(routes.Follow.getfollow(page, 0)).withNewSession.flashing(
			"success", "This user has been unfollowed"
			)
	} //

	//following
	def following(des: String, page: Int) = Action { username =>
		User_Follow.add(username, des)
		Redirect(routes.Follow.getfollow(page, 1)).withNewSession.flashing(
			"success", "This user has been followed"
			)
	}


}