package controllers

import io.circe
import io.circe.parser._
import models.{AccessToken, ArtistList, Error, ErrorDetails, Recommendations, TrackList}
import play.api.cache._
import play.api.libs.json
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
      getAccessToken.fold(redirectToAuthorize) { token =>
        val responseFuture: Future[WSResponse]    = hitApi(myTopArtistsEndpoint, token).get()
        val responseBody: String                  = Await.result(responseFuture, Duration.Inf).body
        val error: Either[circe.Error, Error]     = decode[Error](responseBody)
        val data: Either[circe.Error, ArtistList] = decode[ArtistList](responseBody)
        (error, data) match {
          case (Right(Error(ErrorDetails(401, _))), _) => {
            redirectToAuthorize
          }
          case (Right(Error(ErrorDetails(_, message))), _) => InternalServerError(message)
          case (_, Right(artistList))                      => Ok(views.html.artists("Your Top Artists", artistList.items))
          case _                                           => InternalServerError("Response couldn't be decoded as an error or artist details...")
        }
      }
    }

  private def getTopTracks(accessToken: String): String = {
    val joinedParams                       = joinURLParameters(topTracksParams)
    val endpoint                           = s"$myTopTracksEndpoint?$joinedParams"
    val responseFuture: Future[WSResponse] = hitApi(endpoint, accessToken).get()
    val response: WSResponse               = Await.result(responseFuture, Duration.Inf)
    response.body
  }

  def getMyTopTracks(): Action[AnyContent] =
    Action { implicit request: Request[AnyContent] =>
      getAccessToken.fold(redirectToAuthorize) { token =>
        val topTracksString                                  = getTopTracks(token)
        val error: Either[circe.Error, Error]                = decode[Error](topTracksString)
        val errorOrTrackList: Either[circe.Error, TrackList] = decode[TrackList](topTracksString)

        (error, errorOrTrackList) match {
          case (Right(Error(ErrorDetails(401, _))), _)     => redirectToAuthorize
          case (Right(Error(ErrorDetails(_, message))), _) => InternalServerError(message)
          case (_, Right(trackList)) =>
            Ok(views.html.tracks("Your Top Tracks", trackList.items))
          case _ => InternalServerError("Response couldn't be decoded as an error or artist details...")
        }
      }
    }

  def getRecommendedTracks(): Action[AnyContent] =
    Action { implicit request: Request[AnyContent] =>
      val topTracks: Option[TrackList]               = getCache[TrackList](topTracksCacheKey)
      val recommendedTracks: Option[Recommendations] = getCache[Recommendations](recommendedTracksCacheKey)

      (topTracks, recommendedTracks) match {
        case (Some(trackList), Some(recommendations)) =>
          val seedIds    = recommendations.seeds.map(_.id)
          val seedTracks = trackList.items.filter { track => seedIds.contains(track.id) }
          Ok(views.html.recommendations(seedTracks, recommendations.tracks))
        case _ => Redirect(routes.HomeController.home())
      }
    }

  def saveTrack(trackId: String): Action[AnyContent] =
    Action { implicit request =>
      getAccessToken.fold(redirectToAuthorize) { token =>
        val data = json.Json.obj(
          "ids" -> Seq(trackId.trim)
        )

        hitApi(myTracksEndpoint, token)
          .addHttpHeaders("Content-Type" -> "application/json")
          .put(data)

        Ok

      }
    }

  def refreshRecommendations(): Action[AnyContent] =
    Action { implicit request =>
      getAccessToken.fold(redirectToAuthorize) { token =>
        implicit val accessToken = AccessToken(token)

        getCache[TrackList](topTracksCacheKey) match {
          case None => InternalServerError("Cannot retrieve top tracks from cache")
          case Some(tracks) =>
            cacheRecommendedTracks(tracks)
            Redirect(routes.ApiCallController.getRecommendedTracks())
        }
      }
    }

}
