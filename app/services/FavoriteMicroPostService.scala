package services

import models._
import scalikejdbc.{AutoSession, DBSession}
import skinny.Pagination

import scala.util.Try

trait FavoriteMicroPostService {

  def create(favoriteMicroPost: FavoriteMicroPost)(implicit dbSession: DBSession = AutoSession): Try[Long]

  def findById(microPostId: Long)(implicit dbSession: DBSession = AutoSession): Try[List[FavoriteMicroPost]]

  def findByFavoriteMicroPostId(microPostId: Long)(implicit dbSession: DBSession = AutoSession): Try[Option[FavoriteMicroPost]]

  def findUserByFavoriteMicroPostId(pagination: Pagination, microPostId: Long)(
    implicit dbSession: DBSession = AutoSession
  ): Try[PagedItems[User]]

  def findFavoriteMicroPostByUserId(pagination: Pagination, userId: Long)(
    implicit dbSession: DBSession = AutoSession
  ): Try[PagedItems[MicroPost]]

  def countByUserId(userId: Long)(implicit dbSession: DBSession = AutoSession): Try[Long]

  def countByByFavoriteMicroPostId(favoriteMicroPostId: Long)(implicit dbSession: DBSession = AutoSession): Try[Long]

  def deleteBy(userId: Long, favoriteMicroPostId: Long)(implicit dbSession: DBSession = AutoSession): Try[Int]

}