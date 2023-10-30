package utils

import akka.Done
import io.circe.parser.decode
import models.{AccessToken, Recommendations, SpotifyError, TrackList}
import play.api.cache.AsyncCacheApi
import play.api.http.Status
import play.api.libs.ws.{WSClient, WSResponse}
import utils.ApiMethods.{await, hitApi}
import utils.StringConstants._
import utils.TypeAliases.CirceError

import scala.concurrent.Future
import scala.reflect.ClassTag
object CacheMethods extends Status {

  def getCache[T: ClassTag](key: String)(implicit cache: AsyncCacheApi): Either[SpotifyError, T] =
    await(cache.get[T](key)) match {
      case None    => Left(SpotifyError(INTERNAL_SERVER_ERROR, getCacheErrorMessage(key)))
      case Some(v) => Right(v)
    }

  def setCache[T](key: String, data: T)(implicit cache: AsyncCacheApi): Done = await(cache.set(key, data))

  def cacheTopTracks(implicit
      accessToken: AccessToken,
      ws: WSClient,
      cache: AsyncCacheApi
  ): Either[SpotifyError, Done] = {
    val responseFuture: Future[WSResponse]                    = hitApi(myTopTracksEndpointWithParams).get()
    val topTracksJson: String                                 = await(responseFuture).body
    val circeOrSpotifyError: Either[CirceError, SpotifyError] = decode[SpotifyError](topTracksJson)
    val errorOrTrackList: Either[CirceError, TrackList]       = decode[TrackList](topTracksJson)

    (circeOrSpotifyError, errorOrTrackList) match {
      case (Right(error), _) => Left(error)
      case (_, Right(trackList)) =>
        setCache(topTracksCacheKey, trackList)
        Right(Done)
      case _ =>
        Left(SpotifyError(INTERNAL_SERVER_ERROR, cacheTopTracksErrorMessage))
    }
  }

  def cacheRecommendedTracks(
      topTracks: TrackList
  )(implicit accessToken: AccessToken, ws: WSClient, cache: AsyncCacheApi): Either[SpotifyError, Done] = {

    val topTrackIds: Seq[String] = topTracks.items.take(numberOfTopTrackSeeds).map(_.id)

    val recommendationsFuture: Future[WSResponse] = hitApi(recommendationsEndpointWithParams(topTrackIds)).get()
    val recommendationsJson: String               = await(recommendationsFuture).body

    val circeOrSpotifyError: Either[CirceError, SpotifyError]       = decode[SpotifyError](recommendationsJson)
    val errorOrRecommendations: Either[CirceError, Recommendations] = decode[Recommendations](recommendationsJson)

    (circeOrSpotifyError, errorOrRecommendations) match {
      case (Right(error), _) => Left(error)
      case (_, Right(recommendations)) =>
        setCache(recommendedTracksCacheKey, recommendations)
        Right(Done)
      case _ =>
        Left(
          SpotifyError(INTERNAL_SERVER_ERROR, cacheRecommendedTracksErrorMessage)
        )
    }
  }
}
