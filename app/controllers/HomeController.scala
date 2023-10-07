package controllers

import akka.Done
import models._
import play.api.cache.AsyncCacheApi
import play.api.libs.ws.WSClient
import play.api.mvc._
import utils.Functions._
import utils.StringConstants.topTracksCacheKey

import javax.inject._

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject() (cache: AsyncCacheApi, ws: WSClient, val controllerComponents: ControllerComponents)
    extends BaseController {

  implicit val implicitCache: AsyncCacheApi = cache
  implicit val implicitWs: WSClient         = ws

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
