package utils

import akka.Done
import io.circe
import io.circe.parser.decode
import models.{AccessToken, Error, ErrorDetails, Recommendations, Track, TrackList}
import play.api.cache.AsyncCacheApi
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import play.api.mvc._
import utils.StringConstants._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.reflect.ClassTag

object Functions extends Results {

  def redirectToAuthorize: Result                                     = Redirect(controllers.routes.AuthorizationController.authorize())
  def getAccessToken(implicit request: RequestHeader): Option[AccessToken] = request.session.get(tokenKey).map(AccessToken(_))

  def getCache[T: ClassTag](key: String)(implicit cache: AsyncCacheApi): Either[Error, T] =
    Await.result(cache.get[T](key), Duration.Inf) match {
      //TODO change 500 for proper error code in trait Status
      case None => Left(Error(ErrorDetails(500, s"Could not retrieve item from cache with key: $key")))
      case Some(v) => Right(v)
    }

  def setCache[T](key: String, data: T)(implicit cache: AsyncCacheApi): Done =
    Await.result(cache.set(key, data), Duration.Inf)

  def hitApi(url: String)(implicit accessToken: AccessToken, ws: WSClient): WSRequest =
    ws.url(url)
      .addHttpHeaders("Authorization" -> s"Bearer ${accessToken.access_token}")
      .withRequestTimeout(10000.millis)

  def joinURLParameters(params: Map[String, String]): String = params.map { case (k, v) => s"$k=$v" }.mkString("&")

  def cacheTopTracks(implicit accessToken: AccessToken, ws: WSClient, cache: AsyncCacheApi): Either[Error, Done] = {
    val joinedParams                                     = joinURLParameters(topTracksParams)
    val endpoint                                         = s"$myTopTracksEndpoint?$joinedParams"
    val responseFuture: Future[WSResponse]               = hitApi(endpoint).get()
    val topTracksJson: String                             = Await.result(responseFuture, Duration.Inf).body
    val error: Either[circe.Error, Error]                = decode[Error](topTracksJson)
    val topTracksDecoded: Either[circe.Error, TrackList] = decode[TrackList](topTracksJson)

    (error, topTracksDecoded) match {
      case (Right(error), _) => Left(error)
      case (_, Right(trackList)) =>
        setCache(topTracksCacheKey, trackList)
        Right(Done)
      case _ => Left(Error(ErrorDetails(500, "Couldn't decode response as a known error or track list")))
    }
  }

  def cacheRecommendedTracks(
      topTracks: TrackList
  )(implicit accessToken: AccessToken, ws: WSClient, cache: AsyncCacheApi): Either[Error, Done] = {

    val token = accessToken.access_token

    val seedTracks: Seq[Track] = topTracks.items.take(5)

    val seedTrackIds: Seq[String] = seedTracks.map(_.id)

    val joinedParams                       = joinURLParameters(recommendationsParams(seedTrackIds))
    val endpoint                           = s"$recommendationsEndpoint?$joinedParams"
    val responseFuture: Future[WSResponse] = hitApi(endpoint).get()
    val recommendationsJson: String        = Await.result(responseFuture, Duration.Inf).body

    val error: Either[circe.Error, Error]                     = decode[Error](recommendationsJson)
    val recommendations: Either[circe.Error, Recommendations] = decode[Recommendations](recommendationsJson)

    (error, recommendations) match {
      case (Right(error), _) => Left(error)
      case (_, Right(recommendations)) =>
        setCache(recommendedTracksCacheKey, recommendations)
        Right(Done)
      case _ => Left(Error(ErrorDetails(500, "Couldn't decode response as a known error or recommended tracks")))
    }
  }
}
