package controllers

import akka.Done
import io.circe
import javax.inject.Inject
import play.api.libs.ws._
import play.api.mvc._
import utils.StringConstants.{
  getArtistEndpoint,
  myTopArtistsEndpoint,
  myTopTracksEndpoint,
  recommendationsEndpoint,
  searchApi
}
import utils.Functions.joinURLParameters

import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{Await, Future}
import utils.Functions.{getAccessToken, getAccessTokenUnsafe, redirectToAuthorize}
import io.circe.parser._
import io.circe._
import models.{Artist, ArtistList, Error, ErrorDetails, Recommendations, Track, TrackList}
import play.api.cache._
import play.api.mvc._
import javax.inject.Inject

class ApiCallController @Inject() (
    cache: AsyncCacheApi,
    ws: WSClient,
    val controllerComponents: ControllerComponents
) extends BaseController {

  //TODO set recommended tracks using a cache like this:
  //  val result: Future[Done] = cache.set("item.key", 2)
  //  val futureMaybeUser: Option[Int] = Await.result(cache.get[Int]("item.key"), Duration.Inf)
  //  println(futureMaybeUser)

  def hitApi(url: String, token: String): WSRequest =
    ws.url(url)
      .addHttpHeaders("Authorization" -> s"Bearer $token")
      .withRequestTimeout(10000.millis)

  def processResponse[T: Manifest](
      responseBody: String
  )(title: String, dataToStringSeq: T => Seq[String])(implicit decoder: Decoder[T]) = {
    val error: Either[circe.Error, Error] = decode[Error](responseBody)
    val data: Either[circe.Error, T]      = decode[T](responseBody)
    (error, data) match {
      case (Right(Error(ErrorDetails(401, _))), _)     => redirectToAuthorize
      case (Right(Error(ErrorDetails(_, message))), _) => InternalServerError(message)
      case (_, Right(data: T))                         => Ok(views.html.showArtist(title, dataToStringSeq(data)))
      case _                                           => InternalServerError("Response couldn't be decoded as an error or artist details...")
    }
  }

  def findArtist(accessToken: String, artist: String): Action[AnyContent] =
    Action { implicit request: Request[AnyContent] =>
      {
        def searchURL(query: String) =
          s"${searchApi("Miles Davis&type=artist")}" //"remaster%2520track%3ADoxy%2520artist%3AMies%2520Davis&type=album")}"
        def searchRequest(query: String) =
          hitApi(searchURL(""), accessToken) //todo remove hardcoding
        def searchResponse(query: String): Future[WSResponse] =
          searchRequest("").get()

        Ok(views.html.search("")) //response.body
      }
    }

  def getArtist(artistId: String): Action[AnyContent] =
    Action { implicit request: Request[AnyContent] =>
      {

        val accessToken: String = getAccessTokenUnsafe(request)

        def getArtistURL(artistId: String) = s"$getArtistEndpoint/$artistId"
        def artistRequest(artistId: String): WSRequest =
          hitApi(getArtistURL(artistId), accessToken)
        def artistResponse(artistId: String): Future[WSResponse] =
          artistRequest(artistId).get()

        processResponse[Artist]("")("Get Artist", Artist.convertToStringSeq)
      }
    }

  def getMyTopArtists(): Action[AnyContent] =
    Action { implicit request: Request[AnyContent] =>
      val accessToken: Option[String] = getAccessToken(request)
      accessToken.fold(redirectToAuthorize) { token =>
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
      val accessToken: Option[String] = getAccessToken(request)
      accessToken.fold(redirectToAuthorize) { token =>
        val topTracksString = getTopTracks(token)
        processResponse[TrackList](topTracksString)("Your Top Tracks", TrackList.convertToStringSeq)
      }
    }

  def getRecommendedTracks(): Action[AnyContent] =
    Action { implicit request: Request[AnyContent] =>
      val accessToken: Option[String] = getAccessToken(request)
      accessToken.fold(redirectToAuthorize) { token =>
        val topTracksRaw = getTopTracks(token)

        val error: Either[circe.Error, Error] = decode[Error](topTracksRaw)

        val topTracksDecoded: Either[circe.Error, TrackList] = decode[TrackList](topTracksRaw)

        val seedTracksOrError: Either[circe.Error, Seq[Track]] = topTracksDecoded.map(_.items.take(5))

        val response: Either[circe.Error, String] = seedTracksOrError.map { seedTracks =>
          val seedTrackIds = seedTracks.map(_.id)
          val params = Map(
            "limit"       -> "10", // number of recommendations to return
            "seed_tracks" -> seedTrackIds.mkString(",")
          )
          val joinedParams                       = joinURLParameters(params)
          val endpoint                           = s"$recommendationsEndpoint?$joinedParams"
          val responseFuture: Future[WSResponse] = hitApi(endpoint, token).get()
          Await.result(responseFuture, Duration.Inf).body
        }

        val recommendations: Either[circe.Error, Recommendations] = response.flatMap(decode[Recommendations])

        (error, recommendations, seedTracksOrError) match {
          case (Right(Error(ErrorDetails(401, _))), _, _)     => redirectToAuthorize
          case (Right(Error(ErrorDetails(_, message))), _, _) => InternalServerError(message)
          case (_, Right(recommendations), Right(seedTracks)) =>
            Ok(views.html.recommendations(seedTracks, recommendations.tracks))
          case _ => InternalServerError("Response couldn't be decoded as an error or artist details...")
        }

      }
    }

  def saveTrack(trackId: String): Action[AnyContent] =
    Action { implicit request =>
      println(trackId)
      Continue
    }

}
