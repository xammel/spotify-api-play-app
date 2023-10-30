package utils

import akka.Done
import io.circe
import io.circe.parser.decode
import models.{AccessToken, Error, Recommendations, TrackList}
import play.api.cache.AsyncCacheApi
import play.api.http.Status
import play.api.libs.ws.{WSClient, WSResponse}
import utils.ApiMethods.{await, hitApi}
import utils.StringConstants._

import scala.concurrent.Future
import scala.reflect.ClassTag
object CacheMethods extends Status {

  def getCache[T: ClassTag](key: String)(implicit cache: AsyncCacheApi): Either[Error, T] =
    await(cache.get[T](key)) match {
      case None =>
        Left(Error(INTERNAL_SERVER_ERROR, getCacheErrorMessage(key)))
      case Some(v) => Right(v)
    }

  def setCache[T](key: String, data: T)(implicit cache: AsyncCacheApi): Done =
    await(cache.set(key, data))

  def cacheTopTracks(implicit accessToken: AccessToken, ws: WSClient, cache: AsyncCacheApi): Either[Error, Done] = {
    val responseFuture: Future[WSResponse]               = hitApi(myTopTracksEndpointWithParams).get()
    val topTracksJson: String                            = await(responseFuture).body
    val error: Either[circe.Error, Error]                = decode[Error](topTracksJson)
    val topTracksDecoded: Either[circe.Error, TrackList] = decode[TrackList](topTracksJson)

    (error, topTracksDecoded) match {
      case (Right(error), _) => Left(error)
      case (_, Right(trackList)) =>
        setCache(topTracksCacheKey, trackList)
        Right(Done)
      case _ =>
        Left(Error(INTERNAL_SERVER_ERROR, cacheTopTracksErrorMessage))
    }
  }

  def cacheRecommendedTracks(
      topTracks: TrackList
  )(implicit accessToken: AccessToken, ws: WSClient, cache: AsyncCacheApi): Either[Error, Done] = {

    val topFiveTrackIds: Seq[String] = topTracks.items.take(5).map(_.id)

    val recommendationsFuture: Future[WSResponse] = hitApi(recommendationsEndpointWithParams(topFiveTrackIds)).get()
    val recommendationsJson: String               = await(recommendationsFuture).body

    val error: Either[circe.Error, Error]                     = decode[Error](recommendationsJson)
    val recommendations: Either[circe.Error, Recommendations] = decode[Recommendations](recommendationsJson)

    (error, recommendations) match {
      case (Right(error), _) => Left(error)
      case (_, Right(recommendations)) =>
        setCache(recommendedTracksCacheKey, recommendations)
        Right(Done)
      case _ =>
        Left(
          Error(INTERNAL_SERVER_ERROR, cacheRecommendedTracksErrorMessage)
        )
    }
  }
}
