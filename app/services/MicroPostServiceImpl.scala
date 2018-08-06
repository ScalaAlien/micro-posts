package services

import javax.inject.Singleton

import models.{MicroPost, PagedItems}
import scalikejdbc._
import skinny.Pagination

import scala.util.Try

@Singleton
class MicroPostServiceImpl extends MicroPostService {

  override def create(microPost: MicroPost)(implicit dbSession: DBSession): Try[Long] = Try {
    MicroPost.create(microPost)
  }

  override def deleteById(microPostId: Long)(implicit dbSession: DBSession): Try[Int] = Try {
    MicroPost.deleteById(microPostId)
  }

  override def findByUserId(pagination: Pagination, userId: Long)(
    implicit dbSession: DBSession
  ): Try[PagedItems[MicroPost]] =
    countBy(userId).map { size =>
      PagedItems(pagination, size, findAllByWithLimitOffset(userId)(pagination))
    }

  override def countBy(userId: Long)(implicit dbSession: DBSession): Try[Long] = Try {
    MicroPost.countBy(sqls.eq(MicroPost.defaultAlias.userId, userId))
  }

  override def findAllByWithLimitOffset(pagination: Pagination, userId: Long)(
    implicit dbSession: DBSession
  ): Try[PagedItems[MicroPost]] = Try {
    val size = MicroPost.countBy(sqls.eq(MicroPost.defaultAlias.userId, userId))
    PagedItems(pagination, size, findAllByWithLimitOffset(userId)(pagination))
  }

  private def findAllByWithLimitOffset(userId: Long)(pagination: Pagination)(
    implicit dbSession: DBSession
  ): Seq[MicroPost] = MicroPost.findAllByWithLimitOffset(
    sqls.eq(MicroPost.defaultAlias.userId, userId),
    pagination.limit,
    pagination.offset,
    Seq(MicroPost.defaultAlias.id.desc)
  )

}