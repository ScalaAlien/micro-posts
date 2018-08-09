package models

import java.time.ZonedDateTime

import scalikejdbc._, jsr310._
import skinny.orm._
import skinny.orm.feature._

case class FavoriteMicroPost(id: Option[Long],
                             userId: Long,
                             microPostId: Long,
                             createAt: ZonedDateTime = ZonedDateTime.now(),
                             updateAt: ZonedDateTime = ZonedDateTime.now(),
                             user: Option[User] = None,
                             favoriteMicroPost: Option[MicroPost] = None)

object FavoriteMicroPost extends SkinnyCRUDMapper[FavoriteMicroPost] {

  lazy val u1 = User.createAlias("u1")

  lazy val userRef = belongsToWithAliasAndFkAndJoinCondition[User](
    right = User -> u1,
    fk = "userId",
    on = sqls.eq(defaultAlias.userId, u1.id),
    merge = (fmp, f) => fmp.copy(user = f)
  )

  lazy val mp = MicroPost.createAlias("mp")

  lazy val favoriteRef = belongsToWithAliasAndFkAndJoinCondition[MicroPost](
    right = MicroPost -> mp,
    fk = "microPostId",
    on = sqls.eq(defaultAlias.microPostId, mp.id),
    merge = (fmp, f) => fmp.copy(favoriteMicroPost = f)
  )

  lazy val allAssociations: CRUDFeatureWithId[Long, FavoriteMicroPost] =
    joins(userRef, favoriteRef)

  override def tableName = "favorite_micro_posts"

  override def defaultAlias: Alias[FavoriteMicroPost] = createAlias("fmp")

  override def extract(rs: WrappedResultSet,
                       n: ResultName[FavoriteMicroPost]): FavoriteMicroPost =
    autoConstruct(rs, n, "user", "favoriteMicroPost")

  def create(userFollow: FavoriteMicroPost)(implicit session: DBSession): Long =
    createWithAttributes(toNamedValues(userFollow): _*)

  private def toNamedValues(record: FavoriteMicroPost): Seq[(Symbol, Any)] =
    Seq(
      'userId -> record.userId,
      'microPostId -> record.microPostId,
      'createAt -> record.createAt,
      'updateAt -> record.updateAt
    )

  def update(favoriteMicroPost: FavoriteMicroPost)(
      implicit session: DBSession): Int =
    updateById(favoriteMicroPost.id.get)
      .withAttributes(toNamedValues(favoriteMicroPost): _*)

}
