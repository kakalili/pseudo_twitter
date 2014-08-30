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

object Twitter extends Controller with Secured{

	//defined case class take post
	case class TakePost(postcontent: String)

	//defined form helper
	val takePostForm = Form(
		mapping(
			"postcontent" -> text
			)(TakePost.apply)(TakePost.unapply)
		)
  
  	//defined home link
  	val Home = Redirect(routes.Twitter.twitters(0))

  	//Index action
  	def index = Action {Home}

  	//twitters action
  	def twitters(page: Int) = Action { request =>
      request.session.get("username").map { user =>
        val postTemp = Posts.getLastPost
        val following: Long = User_Follow.page(user, 0, page, 10).total
        val followed: Long = User_Follow.page(user, 1, page, 10).total
        val ps = Posts.userpage(user, page, 10,"post_time", "desc")

        val psTemp: Long = ps.total
        Ok(views.html.twitter(Posts.page(user, page, 10, "post_time", "desc"), following, followed, user, postTemp, psTemp))
      }.getOrElse {
        Redirect(routes.Twitter.index)
      }
  		
  	}

  	//usertwitter action
	def usertwitter(page: Int, name: String) = Action { request =>
      request.session.get("username").map { user =>
        val following: Long = User_Follow.page(user, 0, page, 10).total
        val followed: Long = User_Follow.page(user, 1, page, 10).total
        val ps = Posts.userpage(user, page, 10,"post_time", "desc")

        val psTemp: Long = ps.total
        Ok(views.html.usertwitter(Posts.userpage(name, page, 10, "post_time", "desc"), following, followed, user, psTemp))
      }.getOrElse {
        Redirect(routes.Twitter.index)
      }
  		
  	}  	

  	//del action
    def del(postId: Int, page: Int) = Action { username =>
      Posts.delete(postId)
      Redirect(routes.Twitter.twitters(page)).withNewSession.flashing(
        "success"-> "This post has been deleted"
        )
    }

    //delete user twitter action
    def usertwdel(postId: Int, page: Int) = Action {request =>
      Posts.delete(postId)
      request.session.get("username").map { user =>
        Redirect(routes.Twitter.usertwitter(page, user)).withNewSession.flashing(
        "success" -> "This post has been deleted"
        )
      }.getOrElse {
        Redirect(routes.Twitter.index)
      }
      
    }

    //Add a post action
    def addpost = Action { implicit request =>
      request.session.get("username").map { user =>
        takePostForm.bindFromRequest.fold(
          errors => BadRequest("Oh noes, invalid submission!"
),
          postForm => {
            Posts.addPost(user, postForm.postcontent)
             Home.flashing("success" -> "This post had completed!")
         }
          )
        
      }.getOrElse {
        Redirect(routes.Twitter.index)
      }
      
    }
}