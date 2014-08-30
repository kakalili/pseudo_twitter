package models;

import java.util.{Date}

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

case class Posts(post_id : Int, post_username: String, post_content: String, post_time: String)

/**
* Helper for pagination
*/
case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
	lazy val prev = Option(page - 1).filter(_ >= 0)
	lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}

object Posts{
	val simple = 
		get[Int]("posts.post_id") ~
		get[String]("posts.post_username") ~
		get[String]("posts.post_content") ~
		get[String]("posts.post_time") map {
			case post_id~post_username~post_content~post_time =>
			Posts(post_id, post_username, post_content, post_time)
		}
	//Retrieve all posts
	def findAll : Seq[Posts] = {
		DB.withConnection { implicit connection =>
			SQL("select * from posts").as(Posts.simple *)
		}
	}//end findAll

	//Retrieve a post by author
	def findByUsername(username: String) : Option[Posts] = {
		DB.withConnection { implicit connection =>
			SQL("select * from posts where post_username = {username}").on(
				'username -> username
				).as(Posts.simple.singleOpt)
		}
	} //end 

	//Retrieve a last post of an author
	def findLastPost(username: String) : Posts = {
		DB.withConnection { implicit connection =>
			SQL(
				"""
					select * from posts 
					where post_username = {username}
					and post_time = (select max(posts.post_time) from posts where posts.post_username = {username} )
					limit 0,1
				"""
				).on(
				'username -> username
				).as(Posts.simple.single)
		}
	}//end find last post

	//Retrieve a last post of any post
	def getLastPost : Posts = {
		DB.withConnection { implicit connection =>
			SQL(
				"""
					select * from posts
					where post_time = (select max(posts.post_time) from posts)
					limit 0,1
				"""
				).as(Posts.simple.single)
		}
	} //end get last post

	//Add a post
	def addPost(username: String, post_content: String) {
		DB.withConnection { implicit connection =>
			SQL("insert into posts values({username}, {post_content}, now())").on(
				'username -> username,
				'post_content -> post_content
				).executeUpdate()
		}
	} //end add post

	//Retrieve a list post
	def page(session_name: String, page: Int, pageSize: Int, sortBy: String, order: String) : Page[Posts] = {
		val offset = page * pageSize
		DB.withConnection { implicit connection =>
			val posts = SQL(
				"""
					select * from post
					where post_username in (select distinct user_des_id from user_follow where user_src_id = {session_name} )
					or post_username = {session_name}
					order by {sortBy} {order}
					limit {pageSize} offset {offset}
				"""
				).on(
					'pageSize -> pageSize,
					'offset -> offset,
					'order -> order,
					'sortBy -> sortBy,
					'session_name -> session_name
				).as(Posts.simple *)
			val totalRows = SQL(
					"""
					select * from post
					where post_username in (select distinct user_des_id from user_follow where user_src_id = {session_name} )
					or post_username = {session_name}
					"""
				).on(
					'session_name -> session_name
				).as(scalar[Long].single)
			Page(posts, page, offset, totalRows)
		}
	}//end pagination post 

	//pagination post of an author
	def userpage(session_name: String, page: Int, pageSize: Int, sortBy: String, order: String) : Page[Posts] = {
		val offset = page * pageSize
		DB.withConnection { implicit connection =>
			val posts = SQL(
				"""
					select * from post
					where post_username = {session_name}
					order by {sortBy} {order}
					limit {pageSize} offset {offset}
				"""
				).on(
					'pageSize -> pageSize,
					'offset -> offset,
					'order -> order,
					'sortBy -> sortBy,
					'session_name -> session_name
				).as(Posts.simple *)
			val totalRows = SQL(
					"""
					select * from post
					where post_username = {session_name}
					"""
				).on(
					'session_name -> session_name
				).as(scalar[Long].single)
			Page(posts, page, offset, totalRows)
		}
	} //end pationation post of an author

	//delete post from id
	def delete(id: Int) {
		DB.withConnection { implicit connection =>
			SQL("delete from posts where post_id = {id}").on(
				'id -> id
				).executeUpdate()
		}
	}//end delete function

}//end object