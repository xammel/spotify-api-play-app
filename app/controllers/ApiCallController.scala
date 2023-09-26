package controllers

import io.circe
import io.circe._
import io.circe.parser._
import javax.inject.Inject
import models.{ArtistList, Error, ErrorDetails, Recommendations, TrackList}
import play.api.cache._
import play.api.libs.json
import play.api.libs.ws._
import play.api.mvc._
import utils.Functions._
import utils.StringConstants._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class ApiCallController @Inject() (
    cache: AsyncCacheApi,
    ws: WSClient,
    val controllerComponents: ControllerComponents
) extends BaseController {

  implicit val implicitWs    = ws
  implicit val implicitCache = cache

  def processResponse[T: Manifest](
      responseBody: String
  )(title: String, dataToStringSeq: T => Seq[String])(implicit decoder: Decoder[T]) = {
    val error: Either[circe.Error, Error] = decode[Error](responseBody)
    val data: Either[circe.Error, T]      = decode[T](responseBody)
    (error, data) match {
      case (Right(Error(ErrorDetails(401, _))), _) => {
        redirectToAuthorize
      }
      case (Right(Error(ErrorDetails(_, message))), _) => InternalServerError(message)
      case (_, Right(data: T))                         => Ok(views.html.showListData(title, dataToStringSeq(data)))
      case _                                           => InternalServerError("Response couldn't be decoded as an error or artist details...")
    }
  }

  def getMyTopArtists(): Action[AnyContent] =
    Action { implicit request: Request[AnyContent] =>
      getAccessToken.fold(redirectToAuthorize) { token =>
        val responseFuture: Future[WSResponse] = hitApi(myTopArtistsEndpoint, token).get()
        val response: WSResponse               = Await.result(responseFuture, Duration.Inf)
        processResponse[ArtistList](response.body)("Your Top Artists", ArtistList.convertToStringSeq)
      }
    }

  def getTopTracks(accessToken: String): String = {
    val params = Map(
      "time_range" -> "short_term", // short_term = last 4 weeks, medium_term = last 6 months, long_term = all time
      "limit"      -> "20" // Number of tracks to return
    )
    val joinedParams                       = joinURLParameters(params)
    val endpoint                           = s"$myTopTracksEndpoint?$joinedParams"
    val responseFuture: Future[WSResponse] = hitApi(endpoint, accessToken).get()
    val response: WSResponse               = Await.result(responseFuture, Duration.Inf)
    response.body
  }

  def getMyTopTracks(): Action[AnyContent] =
    Action { implicit request: Request[AnyContent] =>
      getAccessToken.fold(redirectToAuthorize) { token =>
        val topTracksString = getTopTracks(token)
        processResponse[TrackList](topTracksString)("Your Top Tracks", TrackList.convertToStringSeq)
      }
    }

  def getRecommendedTracks(): Action[AnyContent] =
    Action { implicit request: Request[AnyContent] =>
      val topTracks: Option[TrackList]               = getCache[TrackList](topTracksCacheKey)
      val recommendedTracks: Option[Recommendations] = getCache[Recommendations](recommendedTracksCacheKey)

      (topTracks, recommendedTracks) match {
        case (Some(tracks), Some(recommendations)) =>
          Ok(views.html.recommendations(tracks.items, recommendations.tracks))
        //TODO add better handling
        case _ => InternalServerError("Could not fetch cached results for top tracks or recommendations")
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

        Redirect(routes.ApiCallController.getRecommendedTracks())
      }
    }

}
