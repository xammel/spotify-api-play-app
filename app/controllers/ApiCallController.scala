package controllers

import io.circe.parser._
import models.{ArtistList, Recommendations, SpotifyError, TrackList}
import play.api.cache._
import play.api.libs.ws._
import play.api.mvc._
import utils.ActionWithAccessToken
import utils.ApiMethods._
import utils.CacheMethods.{cacheRecommendedTracks, getCache}
import utils.StringConstants._
import utils.TypeAliases._
import utils.NestedFutureHelpers.FutureEitherHelper
import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ApiCallController @Inject() (
    cache: AsyncCacheApi,
    ws: WSClient,
    val controllerComponents: ControllerComponents
) extends BaseController {

  implicit val implicitWs         = ws
  implicit val implicitCache      = cache
  implicit val implicitComponents = controllerComponents

  def getMyTopArtists(): Action[AnyContent] =
    ActionWithAccessToken { implicit accessToken =>
      val topArtistsFuture: Future[WSResponse] = hitApi(myTopArtistsEndpoint).get()
      val topArtistsJsonFuture: Future[String] = topArtistsFuture.map(_.body)
      val circeOrSpotifyErrorFuture: Future[Either[CirceError, SpotifyError]] =
        topArtistsJsonFuture.map(decode[SpotifyError](_))
      val errorOrArtistListFuture: Future[Either[CirceError, ArtistList]] =
        topArtistsJsonFuture.map(decode[ArtistList](_))

      for {
        circeOrSpotifyError <- circeOrSpotifyErrorFuture
        errorOrArtistList   <- errorOrArtistListFuture
      } yield {
        (circeOrSpotifyError, errorOrArtistList) match {
          case (Right(SpotifyError(UNAUTHORIZED, _)), _) => redirectToAuthorize
          case (Right(error), _)                         => InternalServerError(error.message)
          case (_, Right(artistList))                    => Ok(views.html.artists("Your Top Artists", artistList.items))
          case _                                         => InternalServerError("Response couldn't be decoded as an error or artist details...")
        }
      }
    }

  def getMyTopTracks(): Action[AnyContent] =
    ActionWithAccessToken { implicit accessToken =>
      val topTracksFuture: Future[WSResponse] = hitApi(myTopTracksEndpointWithParams).get()
      val topTracksJsonFuture: Future[String] = topTracksFuture.map(_.body)
      val circeOrSpotifyErrorFuture: Future[Either[CirceError, SpotifyError]] =
        topTracksJsonFuture.map(decode[SpotifyError](_))
      val errorOrTrackListFuture: Future[Either[CirceError, TrackList]] = topTracksJsonFuture.map(decode[TrackList](_))

      for {
        circeOrSpotifyError <- circeOrSpotifyErrorFuture
        errorOrTrackList    <- errorOrTrackListFuture
      } yield {
        (circeOrSpotifyError, errorOrTrackList) match {
          case (Right(SpotifyError(UNAUTHORIZED, _)), _) => redirectToAuthorize
          case (Right(error), _)                         => InternalServerError(error.message)
          case (_, Right(trackList))                     => Ok(views.html.tracks("Your Top Tracks", trackList.items))
          case _                                         => InternalServerError("Response couldn't be decoded as an error or artist details...")
        }
      }
    }

  def getRecommendedTracks(): Action[AnyContent] =
    ActionWithAccessToken { _ =>
      val errorOrTopTracksFuture: Future[Either[SpotifyError, TrackList]] = getCache[TrackList](topTracksCacheKey)
      val errorOrRecommendedTracksFuture: Future[Either[SpotifyError, Recommendations]] =
        getCache[Recommendations](recommendedTracksCacheKey)

      for {
        errorOrTopTracks         <- errorOrTopTracksFuture
        errorOrRecommendedTracks <- errorOrRecommendedTracksFuture
      } yield {
        (errorOrTopTracks, errorOrRecommendedTracks) match {
          case (Right(topTracks), Right(recommendedTracks)) =>
            val seedIds    = recommendedTracks.seeds.map(_.id)
            val seedTracks = topTracks.items.filter(track => seedIds.contains(track.id))

            Ok(views.html.recommendations(seedTracks, recommendedTracks.tracks))

          case _ => Redirect(routes.HomeController.home())
        }
      }
    }

  def saveTrack(trackId: String): Action[AnyContent] =
    ActionWithAccessToken { implicit accessToken =>
      val idsToSaveToLibrary = trackIdJson(trackId)

      val myTracksFuture: Future[WSResponse] = hitApi(myTracksEndpoint)
        .addHttpHeaders(CONTENT_TYPE -> JSON)
        .put(idsToSaveToLibrary)

      val myTracksJsonFuture: Future[String] = myTracksFuture.map(_.body)

      myTracksJsonFuture.map(Ok(_))
    }

  def refreshRecommendations(): Action[AnyContent] =
    ActionWithAccessToken { implicit accessToken =>
      getCache[TrackList](topTracksCacheKey).flatMap {
        case Left(error) => Future(InternalServerError(error.message))
        case Right(tracks) =>
          cacheRecommendedTracks(tracks).map { _ =>
            Redirect(routes.ApiCallController.getRecommendedTracks())
          }
      }
    }
}
