package controllers

import akka.Done
import javax.inject._
import models._
import play.api.cache.AsyncCacheApi
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.ws.WSClient
import play.api.mvc._
import utils.Functions.{cacheRecommendedTracks, cacheTopTracks, getAccessToken, redirectToAuthorize, getCache}
import utils.StringConstants.topTracksCacheKey
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
      getAccessToken.fold(redirectToAuthorize) { _ =>
        Redirect(routes.HomeController.home())
      }
    }

  def home(): Action[AnyContent] =
    Action { implicit request: RequestHeader =>
      getAccessToken.fold(redirectToAuthorize) { token =>
        implicit val accessToken: AccessToken = AccessToken(token)

        val cacheTopTracksResult: Either[Error, Done] = cacheTopTracks

        cacheTopTracksResult match {
          case Left(Error(ErrorDetails(401, _))) => redirectToAuthorize
          case Left(error)                       => InternalServerError(error.error.message)
          case Right(_) => {
            val topTracks: Option[TrackList] = getCache[TrackList](topTracksCacheKey)
            topTracks match {
              case None => InternalServerError("Could not fetch cached top tracks")
              case Some(tracks) =>
                val cacheRecommendedTracksResult = cacheRecommendedTracks(tracks)
                cacheRecommendedTracksResult match {
                  case Left(Error(ErrorDetails(401, _))) => redirectToAuthorize
                  case Left(error)                       => InternalServerError(error.error.message)
                  case Right(_)                          => Ok(views.html.home())
                }
            }
          }
        }
      }
    }

}
