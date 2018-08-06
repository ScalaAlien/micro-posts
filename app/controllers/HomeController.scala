package controllers

import javax.inject._

import jp.t2v.lab.play2.auth.OptionalAuthElement
import models.{MicroPost, PagedItems}
import play.api.Logger
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc._
import services.{MicroPostService, UserService}
import skinny.Pagination

@Singleton
class HomeController @Inject()(val userService: UserService,
                               val microPostService: MicroPostService,
                               components: ControllerComponents)
  extends AbstractController(components)
    with I18nSupport
    with AuthConfigSupport
    with OptionalAuthElement {

  private val postForm = Form {
    "content" -> nonEmptyText
  }

  def index(page: Int): Action[AnyContent] = StackAction { implicit request =>
    val userOpt = loggedIn
    userOpt
      .map { user =>
        microPostService
          .findAllByWithLimitOffset(Pagination(10, page), user.id.get)
          .map { pagedItems =>
            Ok(views.html.index(userOpt, postForm, pagedItems))
          }
          .recover {
            case e: Exception =>
              Logger.error(s"occurred error", e)
              Redirect(routes.HomeController.index(page))
                .flashing("failure" -> Messages("InternalError"))
          }
          .getOrElse(InternalServerError(Messages("InternalError")))
      }
      .getOrElse(
        Ok(views.html.index(userOpt, postForm, PagedItems(Pagination(10, page), 0, Seq.empty[MicroPost])))
      )
  }

}