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
import utils.TypeAliases.CirceError

import javax.inject.Inject
import scala.concurrent.Future

class ApiCallController @Inject() (
    cache: AsyncCacheApi,
    ws: WSClient,
    val controllerComponents: ControllerComponents
) extends BaseController {

  implicit val implicitWs         = ws
  implicit val implicitCache      = cache
  implicit val implicitComponents = controllerComponents

  def artists(): Action[AnyContent] =
    ActionWithAccessToken { implicit accessToken =>
      val myTopArtistsFuture: Future[WSResponse]                = hitApi(myTopArtistsEndpoint).get()
      val myTopArtistsJson: String                              = await(myTopArtistsFuture).body
      val circeOrSpotifyError: Either[CirceError, SpotifyError] = decode[SpotifyError](myTopArtistsJson)
      val errorOrArtistList: Either[CirceError, ArtistList]     = decode[ArtistList](myTopArtistsJson)

      (circeOrSpotifyError, errorOrArtistList) match {
        case (Right(SpotifyError(UNAUTHORIZED, _)), _) => redirectToAuthorize
        case (Right(error), _)                         => InternalServerError(error.message)
        case (_, Right(artistList))                    => Ok(views.html.artists("Your Top Artists", artistList.items))
        case _                                         => InternalServerError("Response couldn't be decoded as an error or artist details...")
      }
    }

  def tracks(): Action[AnyContent] =
    ActionWithAccessToken { implicit accessToken =>
      val topTracksFuture: Future[WSResponse]                   = hitApi(myTopTracksEndpointWithParams).get()
      val topTracksJson: String                                 = await(topTracksFuture).body
      val circeOrSpotifyError: Either[CirceError, SpotifyError] = decode[SpotifyError](topTracksJson)
      val errorOrTrackList: Either[CirceError, TrackList]       = decode[TrackList](topTracksJson)

      (circeOrSpotifyError, errorOrTrackList) match {
        case (Right(SpotifyError(UNAUTHORIZED, _)), _) => redirectToAuthorize
        case (Right(error), _)                         => InternalServerError(error.message)
        case (_, Right(trackList))                     => Ok(views.html.tracks("Your Top Tracks", trackList.items))
        case _                                         => InternalServerError("Response couldn't be decoded as an error or artist details...")
      }
    }

  def recommendations(): Action[AnyContent] =
    ActionWithAccessToken { _ =>
      val errorOrTrackList: Either[SpotifyError, TrackList] = getCache[TrackList](topTracksCacheKey)
      val errorOrRecommendations: Either[SpotifyError, Recommendations] =
        getCache[Recommendations](recommendedTracksCacheKey)

      (errorOrTrackList, errorOrRecommendations) match {
        case (Right(trackList), Right(recommendations)) =>
          val seedIds    = recommendations.seeds.map(_.id)
          val seedTracks = trackList.items.filter(track => seedIds.contains(track.id))

          Ok(views.html.recommendations(seedTracks, recommendations.tracks))

        case _ => Redirect(routes.HomeController.home())
      }
    }

  def saveTrack(trackId: String): Action[AnyContent] =
    ActionWithAccessToken { implicit accessToken =>
      val idsToSaveToLibrary = trackIdJson(trackId)

      val myTracksFuture: Future[WSResponse] = hitApi(myTracksEndpoint)
        .addHttpHeaders(CONTENT_TYPE -> JSON)
        .put(idsToSaveToLibrary)

      val myTracksJson: String = await(myTracksFuture).body

      Ok(myTracksJson)
    }

  def refreshRecommendations(): Action[AnyContent] =
    ActionWithAccessToken { implicit accessToken =>
      getCache[TrackList](topTracksCacheKey) match {
        case Left(error) => InternalServerError(error.message)
        case Right(tracks) =>
          cacheRecommendedTracks(tracks)
          Redirect(routes.ApiCallController.recommendations())
      }
    }
}
