package services

import javax.inject.Singleton

import models.{PagedItems, User, FavoriteMicroPost}
import scalikejdbc._
import skinny.Pagination

import scala.util.Try

@Singleton
class FavoriteMicroPostServiceImpl extends FavoriteMicroPostService {

  override def create(userFollow: FavoriteMicroPost)(implicit dbSession: DBSession): Try[Long] = Try {
    FavoriteMicroPost.create(userFollow)
  }

  override def findById(userId: Long)(implicit dbSession: DBSession = AutoSession): Try[List[FavoriteMicroPost]] = Try {
    FavoriteMicroPost.where('userId -> userId).apply()
  }

  override def findByFollowId(followId: Long)(implicit dbSession: DBSession = AutoSession): Try[Option[FavoriteMicroPost]] =
    Try {
      FavoriteMicroPost.where('micro_posts_id -> microPostsId).apply().headOption
    }

  // userIdのユーザーをフォローするユーザーの集合を取得する
  override def findFollowersByUserId(pagination: Pagination, userId: Long)(
    implicit dbSession: DBSession = AutoSession
  ): Try[PagedItems[User]] = {
    countByFollowId(userId).map { size =>
      PagedItems(pagination, size,
        FavoriteMicroPost.allAssociations
          .findAllByWithLimitOffset(
            sqls.eq(FavoriteMicroPost.defaultAlias.microPostsId, userId),
            pagination.limit,
            pagination.offset,
            Seq(FavoriteMicroPost.defaultAlias.id.desc)
          )
          .map(_.user.get)
      )
    }
  }

  override def countByFollowId(userId: Long)(implicit dbSession: DBSession = AutoSession): Try[Long] = Try {
    FavoriteMicroPost.allAssociations.countBy(sqls.eq(FavoriteMicroPost.defaultAlias.microPostsId, userId))
  }

  // userIdのユーザーがフォローしているユーザーの集合を取得する
  override def findFollowingsByUserId(pagination: Pagination, userId: Long)(
    implicit dbSession: DBSession = AutoSession
  ): Try[PagedItems[User]] = {
    // 全体の母数を取得する
    countByUserId(userId).map { size =>
      PagedItems(pagination, size,
        FavoriteMicroPost.allAssociations
          .findAllByWithLimitOffset(
            sqls.eq(FavoriteMicroPost.defaultAlias.userId, userId),
            pagination.limit,
            pagination.offset,
            Seq(FavoriteMicroPost.defaultAlias.id.desc)
          )
          .map(_.followUser.get)
      )
    }
  }

  override def countByUserId(userId: Long)(implicit dbSession: DBSession = AutoSession): Try[Long] = Try {
    FavoriteMicroPost.allAssociations.countBy(sqls.eq(FavoriteMicroPost.defaultAlias.userId, userId))
  }

  override def deleteBy(userId: Long, microPostsId: Long)(implicit dbSession: DBSession = AutoSession): Try[Int] = Try {
    val c     = FavoriteMicroPost.column
    val count = FavoriteMicroPost.countBy(sqls.eq(c.userId, userId).and.eq(c.followId, microPostsId))
    if (count == 1) {
      FavoriteMicroPost.deleteBy(
        sqls
          .eq(FavoriteMicroPost.column.userId, userId)
          .and(sqls.eq(FavoriteMicroPost.column.microPostsId, microPostsId))
      )
    } else 0
  }

}