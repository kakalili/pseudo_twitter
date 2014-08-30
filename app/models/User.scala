package models;

import java.util.{Date}

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

case class User(username: String, fullname: String, passwd: String, email: String )

object User {

	val simple = {
		get[String]("user.username") ~
		get[String]("user.fullname") ~
		get[String]("user.passwd") ~
		get[String]("user.email") map {
			case username~fullname~passwd~email => User(username, fullname, passwd, email)
		}
	}


	def page(session_name: String, page: Int, pageSize: Int, sortBy: String, order: String, filter: String): Page[User] = {
		val offset = page * pageSize

		DB.withConnection { implicit connection =>

			val users = SQL(
				"""
					select * from users
					where username not in (select user_follow.user_des_id from user_follow where user_follow.user_src_id = {session_name} )
					and username != {session_name} 
					and (username like {filter} or fullname like {filter})
					order by {sortBy} {order}
					limit {pageSize} offset {offset}
				"""
				).on(
					'session_name -> session_name,
					'pageSize -> pageSize,
					'offset -> offset,
					'order -> order,
					'filter -> filter,
					'sortBy -> sortBy
				).as(User.simple *)

				val totalRows = SQL(
					"""
						select count(*) from users
						where username not in (select user_follow.user_des_id from user_follow where user_follow.user_src_id = {session_name} )
						and username != {session_name} 
						and (username like {filter} or fullname like {filter})
					"""
					).on(
						'session_name -> session_name,
						'filter -> filter
					).as(scalar[Long].single)

				Page(users, page, offset, totalRows)
		}
	}

	def findByUsername(username: String) : Option[User] = {
		DB.withConnection { implicit connection =>
			SQL("select * from users where username = {username}").on('username -> username).as(User.simple.singleOpt)
		}
	}

	def authenticate(username: String, passwd: String): Option[User] = {
		DB.withConnection { implicit connection =>
			SQL(
				"""
					select * from users
					where username = {username} and passwd = {passwd}
				"""
				).on(
					'username -> username,
					'passwd -> passwd
				).as(User.simple.singleOpt)
		}
	}

	//insert models
	def insert(fullname: String, username: String, passwd: String, email: String) : Boolean = {
		
		DB.withConnection { implicit connection =>
			val b: Boolean = false
			val rowNumbers: Int = SQL("insert into users values({fullname}, {username}, {passwd}, {email})").on(
				'fullname -> fullname,
				'username -> username,
				'passwd -> passwd,
				'email -> email
				).executeUpdate()
			if(rowNumbers > 0) { 
				!b
			} 
			b
		}
	}
}

