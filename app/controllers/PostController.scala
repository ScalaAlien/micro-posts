package controllers

import java.time.ZonedDateTime

import javax.inject.{Inject, Singleton}
import jp.t2v.lab.play2.auth.AuthenticationElement
import models.{FavoriteMicroPost, MicroPost}
import play.api.Logger
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc._
import services._
import skinny.Pagination

@Singleton
class PostController @Inject()(val userService: UserService,
                               val microPostService: MicroPostService,
                               val favoriteMicroPostService: FavoriteMicroPostService,
                               components: ControllerComponents)
  extends AbstractController(components)
    with I18nSupport
    with AuthConfigSupport
    with AuthenticationElement {

  private val postForm = Form {
    "content" -> nonEmptyText
  }

  def post(page: Int): Action[AnyContent] = StackAction { implicit request =>
    val user = loggedIn
    postForm
      .bindFromRequest()
      .fold(
        { formWithErrors =>
          handleError(page, user, formWithErrors)
        }, { content =>
          createMicroPost(page, user, content)
        }
      )
  }

  private def createMicroPost(page: Int,
                              user: User,
                              content: String
                             )(implicit request: RequestHeader) = {
    val now       = ZonedDateTime.now
    val microPost = MicroPost(None, user.id.get, content, now, now)
    microPostService
      .create(microPost)
      .map { _ =>
        Redirect(routes.HomeController.index(page))
      }
      .recover {
        case e: Exception =>
          Logger.error("occurred error", e)
          Redirect(routes.HomeController.index())
            .flashing("failure" -> Messages("InternalError"))
      }
      .getOrElse(InternalServerError(Messages("InternalError")))
  }

  private def handleError(page: Int,
                          user: User,
                          formWithErrors: Form[String]
                         )(implicit request: RequestHeader) = {
    microPostService
      .findAllByWithLimitOffset(Pagination(10, page), user.id.get)
      .map { pagedItems =>
        val favoritePosts = favoriteMicroPostService.findById(user.id.get).get
        BadRequest(views.html.index(Some(user), formWithErrors, pagedItems, favoritePosts))
      }
      .recover {
        case e: Exception =>
          Logger.error("occurred error", e)
          Redirect(routes.HomeController.index())
            .flashing("failure" -> Messages("InternalError"))
      }
      .getOrElse(InternalServerError(Messages("InternalError")))
  }

  def delete(microPostId: Long, page: Int): Action[AnyContent] = StackAction { implicit request =>
    microPostService
      .deleteById(microPostId)
      .map { _ =>
        Redirect(routes.HomeController.index(page))
      }
      .recover {
        case e: Exception =>
          Logger.error("occurred error", e)
          Redirect(routes.HomeController.index())
            .flashing("failure" -> Messages("InternalError"))
      }
      .getOrElse(InternalServerError(Messages("InternalError")))
  }


  def getFavorite(page: Int) = StackAction { implicit request =>
    val triedFavoritePostList = favoriteMicroPostService.findById(loggedIn.id.get)
    val pagination           = Pagination(10, page)
    val triedFavoritePost       = favoriteMicroPostService.findFavoriteMicroPostByUserId(pagination, loggedIn.id.get)
    (for {
      favoritePostList <- triedFavoritePostList
      favoritePost <-  triedFavoritePost
    } yield {
      Ok(
        views.html.common.posts(
          loggedIn,
          favoritePost,
          favoritePostList
        )
      )
    }).recover {
      case e: Exception =>
        Logger.error("occurred error", e)
        Redirect(routes.UsersController.index())
          .flashing("failure" -> Messages("InternalError"))
    }
      .getOrElse(InternalServerError(Messages("InternalError")))
  }
}