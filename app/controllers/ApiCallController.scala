package controllers

import io.circe
import io.circe.parser._
import models.{ArtistList, Error, Recommendations, TrackList}
import play.api.cache._
import play.api.libs.ws._
import play.api.mvc._
import utils.ActionWithAccessToken
import utils.ApiMethods._
import utils.CacheMethods.{cacheRecommendedTracks, getCache}
import utils.StringConstants._

import javax.inject.Inject
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

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
      val myTopArtistsFuture: Future[WSResponse]             = hitApi(myTopArtistsEndpoint).get()
      val myTopArtistsJson: String                           = await(myTopArtistsFuture).body
      val error: Either[circe.Error, Error]                  = decode[Error](myTopArtistsJson)
      val errorOrArtistList: Either[circe.Error, ArtistList] = decode[ArtistList](myTopArtistsJson)

      (error, errorOrArtistList) match {
        case (Right(Error(UNAUTHORIZED, _)), _) => redirectToAuthorize
        case (Right(error), _)                  => InternalServerError(error.message)
        case (_, Right(artistList))             => Ok(views.html.artists("Your Top Artists", artistList.items))
        case _                                  => InternalServerError("Response couldn't be decoded as an error or artist details...")
      }
    }

  def getMyTopTracks(): Action[AnyContent] =
    ActionWithAccessToken { implicit accessToken =>
      val topTracksFuture: Future[WSResponse]              = hitApi(myTopTracksEndpointWithParams).get()
      val topTracksJson: String                            = await(topTracksFuture).body
      val error: Either[circe.Error, Error]                = decode[Error](topTracksJson)
      val errorOrTrackList: Either[circe.Error, TrackList] = decode[TrackList](topTracksJson)

      (error, errorOrTrackList) match {
        case (Right(Error(UNAUTHORIZED, _)), _) => redirectToAuthorize
        case (Right(error), _)                  => InternalServerError(error.message)
        case (_, Right(trackList))              => Ok(views.html.tracks("Your Top Tracks", trackList.items))
        case _                                  => InternalServerError("Response couldn't be decoded as an error or artist details...")
      }
    }

  def getRecommendedTracks(): Action[AnyContent] =
    ActionWithAccessToken { _ =>
      val topTracks: Either[Error, TrackList]               = getCache[TrackList](topTracksCacheKey)
      val recommendedTracks: Either[Error, Recommendations] = getCache[Recommendations](recommendedTracksCacheKey)

      (topTracks, recommendedTracks) match {
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

     val myTracksFuture: Future[WSResponse] =  hitApi(myTracksEndpoint)
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
          Redirect(routes.ApiCallController.getRecommendedTracks())
      }
    }
}
