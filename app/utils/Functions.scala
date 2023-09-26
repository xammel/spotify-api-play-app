package utils

import akka.Done
import io.circe
import io.circe.parser.decode
import models.{AccessToken, Error, ErrorDetails, Recommendations, Track, TrackList}
import play.api.cache.AsyncCacheApi
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import play.api.mvc._
import utils.StringConstants.{myTopTracksEndpoint, recommendationsEndpoint, tokenKey, recommendedTracksCacheKey, topTracksCacheKey}

import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{Await, Future}

object Functions extends Results {

  def redirectToAuthorize: Result                                     = Redirect(controllers.routes.AuthorizationController.authorize())
  def getAccessToken(implicit request: RequestHeader): Option[String] = request.session.get(tokenKey)

  //TODO remove
  def getAccessTokenUnsafe(request: RequestHeader): String =
    request.session.get(tokenKey) match {
      case Some(v) => v
      case None =>
        throw new Exception(
          s"No value found in session state for key $tokenKey"
        )
    }

  def hitApi(url: String, token: String)(implicit ws: WSClient): WSRequest =
    ws.url(url)
      .addHttpHeaders("Authorization" -> s"Bearer $token")
      .withRequestTimeout(10000.millis)

  def joinURLParameters(params: Map[String, String]): String =
    params.map { case (k, v) => s"$k=$v" }.mkString("&")

  def cacheTopTracks(implicit accessToken: AccessToken, ws: WSClient, cache: AsyncCacheApi): Either[Error, Done] = {
    val params = Map(
      "time_range" -> "short_term", // short_term = last 4 weeks, medium_term = last 6 months, long_term = all time
      "limit"      -> "20" // Number of tracks to return
    )
    val joinedParams                                     = joinURLParameters(params)
    val endpoint                                         = s"$myTopTracksEndpoint?$joinedParams"
    val responseFuture: Future[WSResponse]               = hitApi(endpoint, accessToken.access_token).get()
    val topTracksRaw: String                             = Await.result(responseFuture, Duration.Inf).body
    val error: Either[circe.Error, Error]                = decode[Error](topTracksRaw)
    val topTracksDecoded: Either[circe.Error, TrackList] = decode[TrackList](topTracksRaw)

    (error, topTracksDecoded) match {
      case (Right(error), _) => Left(error)
      case (_, Right(trackList)) =>
        Await.result(cache.set(topTracksCacheKey,  trackList), Duration.Inf)
        Right(Done)
      case _ => Left(Error(ErrorDetails(500, "Couldn't decode response as a known error or track list")))
    }
  }

  def cacheRecommendedTracks(
      topTracks: TrackList
  )(implicit accessToken: AccessToken, ws: WSClient, cache: AsyncCacheApi): Either[Error, Done] = {

    val token = accessToken.access_token

    val seedTracks: Seq[Track] = topTracks.items.take(5)

    val seedTrackIds = seedTracks.map(_.id)
    val params = Map(
      "limit"       -> "10", // number of recommendations to return
      "seed_tracks" -> seedTrackIds.mkString(",")
    )
    val joinedParams                       = joinURLParameters(params)
    val endpoint                           = s"$recommendationsEndpoint?$joinedParams"
    val responseFuture: Future[WSResponse] = hitApi(endpoint, token).get()
    val recommendationsJson: String        = Await.result(responseFuture, Duration.Inf).body

    val recommendations: Either[circe.Error, Recommendations] = decode[Recommendations](recommendationsJson)

    recommendations match {
      case Left(decodingError) => Left(Error(ErrorDetails(500, decodingError.getMessage)))
      case Right(recommendations) =>
        Await.result(cache.set(recommendedTracksCacheKey, recommendations), Duration.Inf)
        Right(Done)
    }
  }

}
