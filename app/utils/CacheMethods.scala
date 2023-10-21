package utils

import akka.Done
import io.circe.parser.decode
import models.{AccessToken, Recommendations, SpotifyError, TrackList}
import play.api.cache.AsyncCacheApi
import play.api.http.Status
import play.api.libs.ws.{WSClient, WSResponse}
import utils.ApiMethods.hitApi
import utils.StringConstants._
import utils.TypeAliases.CirceError

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.reflect.ClassTag
object CacheMethods extends Status {

  def getCache[T: ClassTag](key: String)(implicit cache: AsyncCacheApi): Future[Either[SpotifyError, T]] =
    cache.get[T](key).map {
      case None =>
        Left(SpotifyError(INTERNAL_SERVER_ERROR, getCacheErrorMessage(key)))
      case Some(v) => Right(v)
    }

  def cacheTopTracks(implicit
      accessToken: AccessToken,
      ws: WSClient,
      cache: AsyncCacheApi
  ): Future[Either[SpotifyError, Done]] = {
    val topTracksFuture: Future[WSResponse] = hitApi(myTopTracksEndpointWithParams).get()
    val topTracksJsonFuture: Future[String] = topTracksFuture.map(_.body)
    val circeOrSpotifyErrorFuture: Future[Either[CirceError, SpotifyError]] =
      topTracksJsonFuture.map(decode[SpotifyError](_))
    val errorOrTopTracksFuture: Future[Either[CirceError, TrackList]] = topTracksJsonFuture.map(decode[TrackList](_))

    for {
      circeOrSpotifyError   <- circeOrSpotifyErrorFuture
      circeErrorOrTopTracks <- errorOrTopTracksFuture
    } yield {
      (circeOrSpotifyError, circeErrorOrTopTracks) match {
        case (Right(error), _) => Left(error)
        case (_, Right(trackList)) =>
          cache.set(topTracksCacheKey, trackList)
          Right(Done)
        case _ =>
          Left(SpotifyError(INTERNAL_SERVER_ERROR, cacheTopTracksErrorMessage))
      }
    }
  }

  def cacheRecommendedTracks(
      topTracks: TrackList
  )(implicit accessToken: AccessToken, ws: WSClient, cache: AsyncCacheApi): Future[Either[SpotifyError, Done.type]] = {

    val topFiveTrackIds: Seq[String] = topTracks.items.take(5).map(_.id)

    val recommendationsFuture: Future[WSResponse] = hitApi(recommendationsEndpointWithParams(topFiveTrackIds)).get()
    val recommendationsJson: Future[String]       = recommendationsFuture.map(_.body)

    val error: Future[Either[CirceError, SpotifyError]] = recommendationsJson.map(decode[SpotifyError](_))
    val recommendations: Future[Either[CirceError, Recommendations]] =
      recommendationsJson.map(decode[Recommendations](_))

    for {
      circeOrSpotifyError         <- error
      circeErrorOrRecommendations <- recommendations
    } yield {
      (circeOrSpotifyError, circeErrorOrRecommendations) match {
        case (Right(error), _) => Left(error)
        case (_, Right(recommendations)) =>
          cache.set(recommendedTracksCacheKey, recommendations)
          Right(Done)
        case _ =>
          Left(
            SpotifyError(INTERNAL_SERVER_ERROR, cacheRecommendedTracksErrorMessage)
          )
      }
    }
  }
}
