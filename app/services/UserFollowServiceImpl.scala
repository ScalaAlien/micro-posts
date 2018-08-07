package services

import javax.inject.Singleton

import models.{PagedItems, User, UserFollow}
import scalikejdbc._
import skinny.Pagination

import scala.util.Try

@Singleton
class UserFollowServiceImpl extends UserFollowService {

  override def create(userFollow: UserFollow)(implicit dbSession: DBSession): Try[Long] = Try {
    UserFollow.create(userFollow)
  }

  override def findById(userId: Long)(implicit dbSession: DBSession = AutoSession): Try[List[UserFollow]] = Try {
    UserFollow.where('userId -> userId).apply()
  }

  override def findByFollowId(followId: Long)(implicit dbSession: DBSession = AutoSession): Try[Option[UserFollow]] =
    Try {
      UserFollow.where('followId -> followId).apply().headOption
    }

  // userIdのユーザーをフォローするユーザーの集合を取得する
  override def findFollowersByUserId(pagination: Pagination, userId: Long)(
    implicit dbSession: DBSession = AutoSession
  ): Try[PagedItems[User]] = {
    countByFollowId(userId).map { size =>
      PagedItems(pagination, size,
        UserFollow.allAssociations
          .findAllByWithLimitOffset(
            sqls.eq(UserFollow.defaultAlias.followId, userId),
            pagination.limit,
            pagination.offset,
            Seq(UserFollow.defaultAlias.id.desc)
          )
          .map(_.user.get)
      )
    }
  }

  override def countByFollowId(userId: Long)(implicit dbSession: DBSession = AutoSession): Try[Long] = Try {
    UserFollow.allAssociations.countBy(sqls.eq(UserFollow.defaultAlias.followId, userId))
  }

  // userIdのユーザーがフォローしているユーザーの集合を取得する
  override def findFollowingsByUserId(pagination: Pagination, userId: Long)(
    implicit dbSession: DBSession = AutoSession
  ): Try[PagedItems[User]] = {
    // 全体の母数を取得する
    countByUserId(userId).map { size =>
      PagedItems(pagination, size,
        UserFollow.allAssociations
          .findAllByWithLimitOffset(
            sqls.eq(UserFollow.defaultAlias.userId, userId),
            pagination.limit,
            pagination.offset,
            Seq(UserFollow.defaultAlias.id.desc)
          )
          .map(_.followUser.get)
      )
    }
  }

  override def countByUserId(userId: Long)(implicit dbSession: DBSession = AutoSession): Try[Long] = Try {
    UserFollow.allAssociations.countBy(sqls.eq(UserFollow.defaultAlias.userId, userId))
  }

  override def deleteBy(userId: Long, followId: Long)(implicit dbSession: DBSession = AutoSession): Try[Int] = Try {
    val c     = UserFollow.column
    val count = UserFollow.countBy(sqls.eq(c.userId, userId).and.eq(c.followId, followId))
    if (count == 1) {
      UserFollow.deleteBy(
        sqls
          .eq(UserFollow.column.userId, userId)
          .and(sqls.eq(UserFollow.column.followId, followId))
      )
    } else 0
  }

}