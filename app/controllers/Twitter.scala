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

object Twitter extends Controller with Secured{

	//defined case class take post
	case class TakePost(postcontent: String)

	//defined form helper
	val TakePostForm = Form(
		map(
			"postcontent" -> text
			)(TakePost.apply)(TakePost.unapply)
		)
  
  	//defined home link
  	def val Home = Redirect(routes.Twitter.twitters(0))

  	//Index action
  	def index = Action {Home}

  	//twitters action
  	def twitters(page: Int) = Action { username =>
  		val postTemp = Posts.getLastPost
  		val following = User_Follow.page(username, 0, page, 10).getTotalRowCount
  		val followed = User_Follow.page(username, 1, page, 10).getTotalRowCount
  		val ps = Posts.userpage(username, page, 10,"post_time", "desc")

  		val psTemp: Int = ps.getTotalRowCount
  		Ok(views.html.twitter(Posts.page(username, page, 10), following, followed, username, postTemp, psTemp))
  	}

  	//usertwitter action
	def usertwitter(page: Int, name: String) = Action { username =>
  		val following = User_Follow.page(username, 0, page, 10).getTotalRowCount
  		val followed = User_Follow.page(username, 1, page, 10).getTotalRowCount
  		val ps = Posts.userpage(username, page, 10,"post_time", "desc")

  		val psTemp: Int = ps.getTotalRowCount
  		Ok(views.html.usertwitter(Posts.userpage(name, page, 10), following, followed, username, psTemp))
  	}  	

  	//del action
    def del(postId: Int, page: Int) = Action { username =>
      Posts.delete(postId)
      Redirect(routes.Twitter.twitters(page)).withNewSession.flashing(
        "success", "This post has been deleted"
        )
    }

    //delete user twitter action
    def usertwdel(postId: Int, page: Int) = Action {username =>
      Posts.delete(postId)
      Redirect(routes.Twitter.usertwitter(page, username)).withNewSession.flashing(
        "success" , "This post has been deleted"
        )
    }

    //Add a post action
    def addpost = Action { username =>
      val postForm = TakePostForm.bindFromRequest.get
      Posts.addPost(username, postForm.postcontent)
      Home.flashing("success" -> "This post had completed!")
    }
}