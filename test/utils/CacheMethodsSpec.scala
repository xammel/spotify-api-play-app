package utils

import models.{AccessToken, Recommendations, SpotifyError, TrackList}
import play.api.cache.AsyncCacheApi
import spechelpers.SpecHelpers
import utils.ApiMethods.await
import utils.CacheMethods._
import utils.StringConstants._

import scala.concurrent.ExecutionContext.Implicits.global

class CacheMethodsSpec extends SpecHelpers {

  lazy val testKey                       = "testKey"
  lazy implicit val cache: AsyncCacheApi = mockCache
  lazy val token: AccessToken            = AccessToken(testAccessTokenString)

  "getCache" must {
    "return a Left error if the key is not defined in the cache" in {
      val result = for {
        _     <- cache.removeAll()
        value <- getCache[Int](testKey)
      } yield value

      await(result) mustBe Left(SpotifyError(INTERNAL_SERVER_ERROR, getCacheErrorMessage(testKey)))
    }

    "return the Right value from the cache if the key does exist in the cache" in {
      val result = for {
        _   <- cache.removeAll()
        _   <- cache.set(testKey, 2)
        int <- getCache[Int](testKey)
      } yield int

      await(result) mustBe Right(2)
    }
  }

  "cacheTopTracks" must {
    "insert the top tracks into the cache" in {
      val result = for {
        _      <- cache.removeAll()
        _      <- cacheTopTracks(token, mockWS(), cache)
        tracks <- getCache[TrackList](topTracksCacheKey)
      } yield tracks

      await(result) mustBe Right(trackList)
    }

    "return the spotify error if that is returned from the API" in {

      val result = for {
        _      <- cache.removeAll()
        tracks <- cacheTopTracks(token, mockWS(accessTokenIsExpired = true), cache)
      } yield tracks

      await(result) mustBe Left(
        SpotifyError(unauthorizedSpotifyError.error.status, unauthorizedSpotifyError.error.message)
      )
    }

    "return a custom error if the error message cannot be decoded" in {
      val result = for {
        _      <- cache.removeAll()
        tracks <- cacheTopTracks(token, mockWS(returnUnexpectedResponse = true), cache)
      } yield tracks
      await(result) mustBe Left(SpotifyError(INTERNAL_SERVER_ERROR, cacheTopTracksErrorMessage))
    }
  }

  "cacheRecommendedTracks" must {
    "insert the recommended tracks into the cache" in {
      val result = for {
        _               <- cache.removeAll()
        _               <- cacheRecommendedTracks(trackList)(token, mockWS(), cache)
        recommendations <- getCache[Recommendations](recommendedTracksCacheKey)
      } yield recommendations

      await(result) mustBe Right(recommendations)
    }

    "return the spotify error if that is returned from the API" in {
      val result = for {
        _      <- cache.removeAll()
        tracks <- cacheRecommendedTracks(trackList)(token, mockWS(accessTokenIsExpired = true), cache)
      } yield tracks

      await(result) mustBe Left(
        SpotifyError(unauthorizedSpotifyError.error.status, unauthorizedSpotifyError.error.message)
      )
    }

    "return a custom error if the error message cannot be decoded" in {
      val result = for {
        _      <- cache.removeAll()
        tracks <- cacheRecommendedTracks(trackList)(token, mockWS(returnUnexpectedResponse = true), cache)
      } yield tracks
      await(result) mustBe Left(SpotifyError(INTERNAL_SERVER_ERROR, cacheRecommendedTracksErrorMessage))
    }
  }

}
