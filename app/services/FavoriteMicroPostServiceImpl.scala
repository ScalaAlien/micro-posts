package services

import javax.inject.Singleton
import models.{FavoriteMicroPost, MicroPost, PagedItems, User}
import scalikejdbc._
import skinny.Pagination

import scala.util.Try

@Singleton
class FavoriteMicroPostServiceImpl extends FavoriteMicroPostService {

  override def create(favoriteMicroPost: FavoriteMicroPost)(implicit dbSession: DBSession): Try[Long] = Try {
    FavoriteMicroPost.create(favoriteMicroPost)
  }

  override def findById(userId: Long)(implicit dbSession: DBSession = AutoSession): Try[List[FavoriteMicroPost]] = Try {
    FavoriteMicroPost.where('userId -> userId).apply()
  }

  override def findByFavoriteMicroPostId(microPostId: Long)(implicit dbSession: DBSession = AutoSession): Try[Option[FavoriteMicroPost]] =
    Try {
      FavoriteMicroPost.where('microPostId -> microPostId).apply().headOption
    }

  // userIdのユーザーをフォローするユーザーの集合を取得する
  override def findUserByFavoriteMicroPostId(pagination: Pagination, microPostId: Long)(
    implicit dbSession: DBSession = AutoSession
  ): Try[PagedItems[User]] = {
    countByByFavoriteMicroPostId(microPostId).map { size =>
      PagedItems(pagination, size,
        FavoriteMicroPost.allAssociations
          .findAllByWithLimitOffset(
            sqls.eq(FavoriteMicroPost.defaultAlias.microPostId, microPostId),
            pagination.limit,
            pagination.offset,
            Seq(FavoriteMicroPost.defaultAlias.id.desc)
          )
          .map(_.user.get)
      )
    }
  }

  // userIdのユーザーがフォローしているユーザーの集合を取得する
  override def findFavoriteMicroPostByUserId(pagination: Pagination, userId: Long)(
    implicit dbSession: DBSession = AutoSession
  ): Try[PagedItems[MicroPost]] = {
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
          .map(_.favoriteMicroPost.get)
      )
    }
  }

  override def countByUserId(userId: Long)(implicit dbSession: DBSession = AutoSession): Try[Long] = Try {
    FavoriteMicroPost.allAssociations.countBy(sqls.eq(FavoriteMicroPost.defaultAlias.userId, userId))
  }

  override def countByByFavoriteMicroPostId(favoriteMicroPostId: Long)(implicit dbSession: DBSession = AutoSession): Try[Long] = Try {
    FavoriteMicroPost.allAssociations.countBy(sqls.eq(FavoriteMicroPost.defaultAlias.microPostId, favoriteMicroPostId))
  }

  override def deleteBy(userId: Long, favoriteMicroPostId: Long)(implicit dbSession: DBSession = AutoSession): Try[Int] = Try {
    val c = FavoriteMicroPost.column
    val count = FavoriteMicroPost.countBy(sqls.eq(c.userId, userId).and.eq(c.microPostId, favoriteMicroPostId))
    if (count == 1) {
      FavoriteMicroPost.deleteBy(
        sqls
          .eq(FavoriteMicroPost.column.userId, userId)
          .and(sqls.eq(FavoriteMicroPost.column.microPostId, favoriteMicroPostId))
      )
    } else 0
  }

}