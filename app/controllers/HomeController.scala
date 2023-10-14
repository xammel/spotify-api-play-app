package controllers

import akka.Done
import models._
import play.api.cache.AsyncCacheApi
import play.api.libs.ws.WSClient
import play.api.mvc._
import utils.ActionWithAccessToken
import utils.ApiMethods._
import utils.CacheMethods.{cacheRecommendedTracks, cacheTopTracks, getCache}
import utils.StringConstants.topTracksCacheKey

import javax.inject._

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject() (cache: AsyncCacheApi, ws: WSClient, val controllerComponents: ControllerComponents)
    extends BaseController {

  implicit val implicitCache: AsyncCacheApi             = cache
  implicit val implicitWs: WSClient                     = ws
  implicit val implicitComponents: ControllerComponents = controllerComponents

  def home(): Action[AnyContent] =
    ActionWithAccessToken { implicit accessToken =>
      val cacheTopTracksResult: Either[Error, Done] = cacheTopTracks

      val topTracks: Either[Error, TrackList] =
        cacheTopTracksResult.flatMap(_ => getCache[TrackList](topTracksCacheKey))

      val cacheRecommendedTracksResult: Either[Error, Done] = topTracks.flatMap(cacheRecommendedTracks(_))

      cacheRecommendedTracksResult match {
        case Left(Error(UNAUTHORIZED, _)) => redirectToAuthorize
        case Left(error)                  => InternalServerError(error.message)
        case Right(_)                     => Ok(views.html.home())
      }
    }

}
