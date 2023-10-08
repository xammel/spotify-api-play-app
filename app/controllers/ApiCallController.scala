package controllers

import io.circe
import io.circe.parser._
import models.{AccessToken, ArtistList, Error, ErrorDetails, Recommendations, TrackList}
import play.api.cache._
import play.api.libs.ws._
import play.api.mvc._
import utils.Functions._
import utils.StringConstants._

import javax.inject.Inject
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class ApiCallController @Inject() (
    cache: AsyncCacheApi,
    ws: WSClient,
    val controllerComponents: ControllerComponents
) extends BaseController {

  implicit val implicitWs    = ws
  implicit val implicitCache = cache

  def getMyTopArtists(): Action[AnyContent] =
    Action { implicit request: Request[AnyContent] =>
      getAccessToken.fold(redirectToAuthorize) { implicit accessToken =>

        val myTopArtistsFuture: Future[WSResponse]             = hitApi(myTopArtistsEndpoint).get()
        val myTopArtistsJson: String                           = Await.result(myTopArtistsFuture, Duration.Inf).body
        val error: Either[circe.Error, Error]                  = decode[Error](myTopArtistsJson)
        val errorOrArtistList: Either[circe.Error, ArtistList] = decode[ArtistList](myTopArtistsJson)

        (error, errorOrArtistList) match {
          case (Right(Error(ErrorDetails(UNAUTHORIZED, _))), _) => redirectToAuthorize
          case (Right(error), _)                                => InternalServerError(error.error.message)
          case (_, Right(artistList))                           => Ok(views.html.artists("Your Top Artists", artistList.items))
          case _                                                => InternalServerError("Response couldn't be decoded as an error or artist details...")
        }
      }
    }

  private def getTopTracksJson(implicit accessToken: AccessToken): String = {
    val joinedParams                       = joinURLParameters(topTracksParams)
    val endpoint                           = s"$myTopTracksEndpoint?$joinedParams"
    val responseFuture: Future[WSResponse] = hitApi(endpoint).get()

    Await.result(responseFuture, Duration.Inf).body
  }

  def getMyTopTracks(): Action[AnyContent] =
    Action { implicit request: Request[AnyContent] =>
      getAccessToken.fold(redirectToAuthorize) { implicit accessToken =>
        val topTracksJson                                    = getTopTracksJson
        val error: Either[circe.Error, Error]                = decode[Error](topTracksJson)
        val errorOrTrackList: Either[circe.Error, TrackList] = decode[TrackList](topTracksJson)

        (error, errorOrTrackList) match {
          case (Right(Error(ErrorDetails(UNAUTHORIZED, _))), _) => redirectToAuthorize
          case (Right(error), _)                                => InternalServerError(error.error.message)
          case (_, Right(trackList))                            => Ok(views.html.tracks("Your Top Tracks", trackList.items))
          case _                                                => InternalServerError("Response couldn't be decoded as an error or artist details...")
        }
      }
    }

  def getRecommendedTracks(): Action[AnyContent] =
    Action { implicit request: Request[AnyContent] =>
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
    Action { implicit request =>
      getAccessToken.fold(redirectToAuthorize) { implicit accessToken =>
        val idsToSaveToLibrary = trackIdJson(trackId)

        hitApi(myTracksEndpoint)
          .addHttpHeaders("Content-Type" -> "application/json")
          .put(idsToSaveToLibrary)

        Ok
      }
    }

  def refreshRecommendations(): Action[AnyContent] =
    Action { implicit request =>
      getAccessToken.fold(redirectToAuthorize) { implicit accessToken =>

        getCache[TrackList](topTracksCacheKey) match {
          case Left(_) => InternalServerError("Cannot retrieve top tracks from cache")
          case Right(tracks) =>
            cacheRecommendedTracks(tracks)
            Redirect(routes.ApiCallController.getRecommendedTracks())
        }
      }
    }

}
