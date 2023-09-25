package controllers

import akka.Done
import javax.inject._
import models.{AccessToken, ArtistId, Error, ErrorDetails, Recommendations, TrackList}
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.mvc._
import utils.Functions.getAccessToken
import play.api.cache.AsyncCacheApi
import utils.Functions.redirectToAuthorize
import play.api.data.Form
import play.api.libs.ws.WSClient
import utils.Functions.{cacheRecommendedTracks, cacheTopTracks}
import utils.StringConstants.tokenKey

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject() (cache: AsyncCacheApi, ws: WSClient, cc: ControllerComponents)
    extends AbstractController(cc)
    with I18nSupport {

  implicit val implicitCache: AsyncCacheApi = cache
  implicit val wsClient: WSClient           = ws

  def index() =
    Action { implicit request: Request[AnyContent] =>
      val accessTokenOpt = getAccessToken(request)
      accessTokenOpt match {
        case Some(_) => Redirect(routes.HomeController.home())
        case None    => Redirect(routes.AuthorizationController.authorize())
      }
    }

  def home(artistIdForm: Form[ArtistId] = ArtistId.artistIdForm.bind(Map("artistId" -> ""))): Action[AnyContent] =
    Action { implicit request: RequestHeader =>
      val accessTokenOpt: Option[String] = getAccessToken

      //TODO remove
      println("access token thing", accessTokenOpt)

      accessTokenOpt.fold(redirectToAuthorize) { token =>
        implicit val accessToken: AccessToken = AccessToken(token)

        val cacheTopTracksResult: Either[models.Error, Done] = cacheTopTracks

        cacheTopTracksResult match {
          case Left(Error(ErrorDetails(401, _))) => redirectToAuthorize
          case Left(error)                       => InternalServerError(error.error.message)
          case Right(_) => {
            val topTracks: Option[TrackList] = Await.result(cache.get[TrackList]("topTracks"), Duration.Inf)
            topTracks match {
              case None => InternalServerError("Could not fetch cached top tracks")
              case Some(tracks) =>
                val cacheRecommendedTracksResult = cacheRecommendedTracks(tracks)
                cacheRecommendedTracksResult match {
                  case Left(Error(ErrorDetails(401, _))) => redirectToAuthorize
                  case Left(error)                       => InternalServerError(error.error.message)
                  case Right(_)                          => Ok(views.html.home(artistIdForm))
                }
            }
          }
        }
      }
    }

}
