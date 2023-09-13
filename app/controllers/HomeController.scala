package controllers

import javax.inject._
import models.ArtistId
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.mvc._
import utils.Functions.getAccessToken

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject()(cc: ControllerComponents)
    extends AbstractController(cc) with I18nSupport{

  /**
    * Create an Action to render an HTML page.
    *
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
  def index() = Action { implicit request: Request[AnyContent] =>
  val accessTokenOpt = getAccessToken(request)
    accessTokenOpt match {
      case Some(_) => Redirect(routes.HomeController.home())
      case None => Redirect(routes.AuthorizationController.authorize())
    }
  }

  val artistIdForm = Form(
    mapping("artistId" -> text)(ArtistId.apply)(ArtistId.unapply)
  )

  def home(artistIdForm: Form[ArtistId] = artistIdForm.bind(Map("artistId" -> ""))) = Action {
    implicit request: RequestHeader =>
      Ok(views.html.home(artistIdForm))
  }

  def explore() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.explore())
  }

  def tutorial() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.tutorial())
  }

  def hello(name: String): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      Ok(views.html.hello(name))
  }

  def d3Test(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      Ok(views.html.d3())
  }

}
