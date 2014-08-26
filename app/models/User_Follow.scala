package models;

import java.util.{Date}

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

case class User_Follow(user_follow_id: Int, user_src_id: String, user_des_id: String)

/**
* parse a user_follow from ResultSet
*/
object User_Follow {
	val simple = {
		get[Int]("user_follow.user_follow_id") ~
		get[String]("user_follow.user_src_id") ~
		get[String]("user_follow.user_des_id") map {
			case user_follow_id~user_src_id~user_des_id => User_Follow(user_follow_id, user_src_id, user_des_id)
		}
	}
	//Find all user_follow
	def findAll: Seq[User_Follow] = {
		DB.withConnection { implicit connection =>
			SQL("select * from user_follow").as(User_Follow.simple *)
		}
	}

	//Retrieve User
	//def getUser(username: String): Option[User] = {
	//	DB.withConnection { implicit connection =>
	//		SQL("select")
	//	}
	//}

	//Pagination User_Follow
	def page(session_name: String, tp: Int, page: Int, pageSize: Int) : Page[User_Follow] = {
		val offset = page * pageSize
		if(tp == 0) {
			DB.withConnection { implicit connection =>
				val user_follow = SQL(
					"""
						select * from user_follow
						where user_src_id = {session_name}
						limit {pageSize} offset {offset}
					"""
					).on(
						'pageSize -> pageSize,
						'offset -> offset,
						'session_name -> session_name
					).as(User_Follow.simple *)
				val totalRows = SQL(
					"""
						select count(*) from user_follow
						where where user_src_id = {session_name}

					"""
					).on(
						'session_name -> session_name
					).as(scalar[Long].single)
				Page(user_follow, page, offset, totalRows)
			}
		}//end if
		else {
			DB.withConnection { implicit connection =>
				val user_follow = SQL(
					"""
						select * from user_follow
						where user_src_id not in (select distinct user_follow.user_des_id from user_follow where user_follow.user_src_id = {session_name} )
						and user_des_id = {session_name}
						limit {pageSize} offset {offset}
					"""
					).on(
						'pageSize -> pageSize,
						'offset -> offset,
						'session_name -> session_name
					).as(User_Follow.simple *)
				val totalRows = SQL(
					"""
						select * from user_follow
						where user_src_id not in (select distinct user_follow.user_des_id from user_follow where user_follow.user_src_id = {session_name} )
						and user_des_id = {session_name}
					"""
					).on(
						'session_name -> session_name
					).as(scalar[Long].single)
				Page(user_follow, page, offset, totalRows)
			}
		}//end else
	}

	//Add a relation to user_follow table
	def add(id_src: String, id_des: String) {
		DB.withConnection { implicit connection =>
			SQL("inset into user_follow values({id_src}, {id_des})").on(
				'id_src -> id_src,
				'id_des -> id_des
				).executeUpdate()
		}
	}//end function add

	//Delete a way of follow
	def delete(id: Int) {
		DB.withConnection { implicit connection =>
			SQL("delete from user_follow where user_follow_id = {id}").on(
				'id -> id
				).executeUpdate()
		}
	} //end function delete
}