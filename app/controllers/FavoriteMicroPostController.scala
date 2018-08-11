package controllers

import java.time.ZonedDateTime

import javax.inject._
import jp.t2v.lab.play2.auth.AuthenticationElement
import models.{FavoriteMicroPost, MicroPost, PagedItems}
import play.api.Logger
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc._
import services.{FavoriteMicroPostService, UserService}
import skinny.Pagination

@Singleton
class FavoriteMicroPostController @Inject()(val favoriteMicroPostService: FavoriteMicroPostService,
                                            val userService: UserService,
                                            components: ControllerComponents)
  extends AbstractController(components)
    with I18nSupport
    with AuthConfigSupport
    with AuthenticationElement {

  def index(page: Int): Action[AnyContent] = StackAction { implicit request =>
    val currentUser: User = loggedIn
    val triedFavoritePosts = favoriteMicroPostService.findFavoriteMicroPostByUserId(Pagination(10,page),currentUser.id.get)
    (for {
      favoritePosts: PagedItems[MicroPost] <- triedFavoritePosts
    } yield {
      Ok(
        views.html.micro_post_favorite.favorite_button(
          loggedIn,
          favoritePosts
        )
      )
    })
      .recover {
        case e: Exception =>
          Logger.error("occurred error", e)
          Redirect(routes.FavoriteMicroPostController.index(10))
            .flashing("failure" -> Messages("InternalError"))
      }
      .getOrElse(InternalServerError(Messages("InternalError")))
  }

  def favorite(microPostId: Long): Action[AnyContent] = StackAction { implicit request =>
    val currentUser = loggedIn
    val now = ZonedDateTime.now()
    val favoriteMicroPost = FavoriteMicroPost(None, currentUser.id.get, microPostId, now, now)
    favoriteMicroPostService
      .create(favoriteMicroPost)
      .map { _ =>
        Redirect(routes.HomeController.index())
      }
      .recover {
        case e: Exception =>
          Logger.error("occurred error", e)
          Redirect(routes.HomeController.index())
            .flashing("failure" -> Messages("InternalError"))
      }
      .getOrElse(InternalServerError(Messages("InternalError")))
  }

  def unFavorite(microPostId: Long): Action[AnyContent] = StackAction { implicit request =>
    val currentUser = loggedIn
    favoriteMicroPostService
      .deleteBy(currentUser.id.get, microPostId)
      .map { _ =>
        Redirect(routes.HomeController.index())
      }
      .recover {
        case e: Exception =>
          Logger.error("occurred error", e)
          Redirect(routes.HomeController.index())
            .flashing("failure" -> Messages("InternalError"))
      }
      .getOrElse(InternalServerError(Messages("InternalError")))
  }

}