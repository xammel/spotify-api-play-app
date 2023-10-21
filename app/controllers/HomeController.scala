package controllers

import akka.Done
import models._
import play.api.cache.AsyncCacheApi
import play.api.libs.ws.WSClient
import play.api.mvc._
import utils.ActionWithAccessToken
import utils.ApiMethods._
import utils.CacheMethods.{cacheRecommendedTracks, cacheTopTracks, getCache}
import utils.NestedFutureHelpers.FutureEitherHelper
import utils.StringConstants.topTracksCacheKey

import javax.inject._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

//TODO can refactor the whole app to be written in a non-blocking way with Action.async and returning Future[Result]

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
      val cacheTopTracksFuture: Future[Either[SpotifyError, Done]] = cacheTopTracks

      val errorOrTopTracksFuture: Future[Either[SpotifyError, TrackList]] =
        cacheTopTracksFuture.preserveErrorsAndFlatMap(_ => getCache[TrackList](topTracksCacheKey))

      val cacheRecommendedTracksFuture: Future[Either[SpotifyError, Done]] =
        errorOrTopTracksFuture.preserveErrorsAndFlatMap(cacheRecommendedTracks(_))

      cacheRecommendedTracksFuture.map {
        case Left(SpotifyError(UNAUTHORIZED, _)) => redirectToAuthorize
        case Left(error)                         => InternalServerError(error.message)
        case Right(_)                            => Ok(views.html.home())
      }
    }

}
