package utils

import akka.Done
import io.circe
import io.circe.parser.decode
import models.{AccessToken, Error, ErrorDetails, Recommendations, TrackList}
import play.api.cache.AsyncCacheApi
import play.api.http.Status
import play.api.libs.ws.{WSClient, WSResponse}
import utils.ApiMethods.{hitApi, joinURLParameters}
import utils.StringConstants._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.reflect.ClassTag
object CacheMethods extends Status {

  def getCache[T: ClassTag](key: String)(implicit cache: AsyncCacheApi): Either[Error, T] =
    Await.result(cache.get[T](key), Duration.Inf) match {
      case None =>
        Left(Error(ErrorDetails(INTERNAL_SERVER_ERROR, s"Could not retrieve item from cache with key: $key")))
      case Some(v) => Right(v)
    }

  def setCache[T](key: String, data: T)(implicit cache: AsyncCacheApi): Done =
    Await.result(cache.set(key, data), Duration.Inf)

  def cacheTopTracks(implicit accessToken: AccessToken, ws: WSClient, cache: AsyncCacheApi): Either[Error, Done] = {
    val responseFuture: Future[WSResponse]               = hitApi(myTopTracksEndpointWithParams).get()
    val topTracksJson: String                            = Await.result(responseFuture, Duration.Inf).body
    val error: Either[circe.Error, Error]                = decode[Error](topTracksJson)
    val topTracksDecoded: Either[circe.Error, TrackList] = decode[TrackList](topTracksJson)

    (error, topTracksDecoded) match {
      case (Right(error), _) => Left(error)
      case (_, Right(trackList)) =>
        setCache(topTracksCacheKey, trackList)
        Right(Done)
      case _ =>
        Left(Error(ErrorDetails(INTERNAL_SERVER_ERROR, "Couldn't decode response as a known error or track list")))
    }
  }

  def cacheRecommendedTracks(
      topTracks: TrackList
  )(implicit accessToken: AccessToken, ws: WSClient, cache: AsyncCacheApi): Either[Error, Done] = {

    val topFiveTrackIds: Seq[String] = topTracks.items.take(5).map(_.id)

    val joinedParams                              = joinURLParameters(recommendationsParams(topFiveTrackIds))
    val recommendationsFuture: Future[WSResponse] = hitApi(s"$recommendationsEndpoint?$joinedParams").get()
    val recommendationsJson: String               = Await.result(recommendationsFuture, Duration.Inf).body

    val error: Either[circe.Error, Error]                     = decode[Error](recommendationsJson)
    val recommendations: Either[circe.Error, Recommendations] = decode[Recommendations](recommendationsJson)

    (error, recommendations) match {
      case (Right(error), _) => Left(error)
      case (_, Right(recommendations)) =>
        setCache(recommendedTracksCacheKey, recommendations)
        Right(Done)
      case _ =>
        Left(
          Error(ErrorDetails(INTERNAL_SERVER_ERROR, "Couldn't decode response as a known error or recommended tracks"))
        )
    }
  }
}
