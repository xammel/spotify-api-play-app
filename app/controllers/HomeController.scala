package controllers

import javax.inject._
import models.{AccessToken, ArtistId, TrackList}
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.mvc._
import utils.Functions.getAccessToken
import play.api.cache.AsyncCacheApi
import utils.Functions.redirectToAuthorize
import play.api.data.Form

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject() (cache: AsyncCacheApi, cc: ControllerComponents)
    extends AbstractController(cc)
    with I18nSupport {

  def index() =
    Action { implicit request: Request[AnyContent] =>
      val accessTokenOpt = getAccessToken(request)
      accessTokenOpt match {
        case Some(_) => Redirect(routes.HomeController.home())
        case None    => Redirect(routes.AuthorizationController.authorize())
      }
    }

  def home(artistIdForm: Form[ArtistId] = ArtistId.artistIdForm.bind(Map("artistId" -> ""))) =
    Action { implicit request: RequestHeader =>

      val accessTokenOpt: Option[String] = getAccessToken

      accessTokenOpt match {
        case None => redirectToAuthorize
        case Some(token) =>
          implicit val accessToken: AccessToken = AccessToken(token)
          routes.ApiCallController.cacheTopTracks()
          val topTracks: Option[TrackList] = Await.result(cache.get[TrackList]("topTracks"), Duration.Inf)
          topTracks match {
            case None => InternalServerError("No top tracks were found in the cache")
            case Some(tracks) => routes.ApiCallController.cacheRecommendedTracks(tracks)
          }
      }

      Ok(views.html.home(artistIdForm))
    }

}
