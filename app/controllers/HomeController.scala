package controllers

import javax.inject._
import models.ArtistId
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.mvc._
import utils.Functions.getAccessToken
import models.ArtistId

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject()(cc: ControllerComponents)
    extends AbstractController(cc) with I18nSupport{

  def index() = Action { implicit request: Request[AnyContent] =>
  val accessTokenOpt = getAccessToken(request)
    accessTokenOpt match {
      case Some(_) => Redirect(routes.HomeController.home())
      case None => Redirect(routes.AuthorizationController.authorize())
    }
  }

  def home(artistIdForm: Form[ArtistId] = ArtistId.artistIdForm.bind(Map("artistId" -> ""))) = Action {
    implicit request: RequestHeader =>
      Ok(views.html.home(artistIdForm))
  }

}
