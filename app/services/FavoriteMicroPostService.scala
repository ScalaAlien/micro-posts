package services

import models.{PagedItems, User, FavoriteMicroPost}
import scalikejdbc.{AutoSession, DBSession}
import skinny.Pagination

import scala.util.Try

trait FavoriteMicroPostService {

  def create(favoriteMicroPost: FavoriteMicroPost)(implicit dbSession: DBSession = AutoSession): Try[Long]

  def findById(userId: Long)(implicit dbSession: DBSession = AutoSession): Try[List[FavoriteMicroPost]]

  def findByFollowId(followId: Long)(implicit dbSession: DBSession = AutoSession): Try[Option[FavoriteMicroPost]]

  def findFollowersByUserId(pagination: Pagination, userId: Long)(
    implicit dbSession: DBSession = AutoSession
  ): Try[PagedItems[User]]

  def findFollowingsByUserId(pagination: Pagination, userId: Long)(
    implicit dbSession: DBSession = AutoSession
  ): Try[PagedItems[User]]

  def countByUserId(userId: Long)(implicit dbSession: DBSession = AutoSession): Try[Long]

  def countFavoriteMicroPostId(userId: Long)(implicit dbSession: DBSession = AutoSession): Try[Long]

  def deleteBy(userId: Long, followId: Long)(implicit dbSession: DBSession = AutoSession): Try[Int]

}