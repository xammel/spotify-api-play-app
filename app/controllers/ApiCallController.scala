package controllers

import akka.Done
import io.circe
import io.circe._
import io.circe.parser._
import javax.inject.Inject
import models.{AccessToken, Artist, ArtistList, Error, ErrorDetails, Recommendations, Track, TrackList}
import play.api.cache._
import play.api.libs.ws._
import play.api.mvc._
import utils.Functions.{getAccessToken, getAccessTokenUnsafe, joinURLParameters, redirectToAuthorize}
import utils.StringConstants._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{Await, ExecutionContext, Future}

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

  def cacheTopTracks(implicit accessToken: AccessToken) = {
    val params = Map(
      "time_range" -> "short_term", // short_term = last 4 weeks, medium_term = last 6 months, long_term = all time
      "limit"      -> "20" // Number of tracks to return
    )
    val joinedParams                       = joinURLParameters(params)
    val endpoint                           = s"$myTopTracksEndpoint?$joinedParams"
    val responseFuture: Future[WSResponse] = hitApi(endpoint, accessToken.access_token).get()
    val topTracksRaw: String               = Await.result(responseFuture, Duration.Inf).body
    val error: Either[circe.Error, Error] = decode[Error](topTracksRaw)
    val topTracksDecoded: Either[circe.Error, TrackList] = decode[TrackList](topTracksRaw)

    (error, topTracksDecoded) match {
      case (Right(Error(ErrorDetails(401, _))), _) => redirectToAuthorize
      case (Right(Error(ErrorDetails(_, message))), _) => InternalServerError(message)
      case (_, Right(trackList)) =>
        Await.result(cache.set("topTracks", trackList), Duration.Inf)
        Ok
      case _ => InternalServerError("...")
    }
  }

  def getMyTopTracks(): Action[AnyContent] =
    Action { implicit request: Request[AnyContent] =>
      val accessToken: Option[String] = getAccessToken(request)
      accessToken.fold(redirectToAuthorize) { token =>
        val topTracksString = getTopTracks(token)
        processResponse[TrackList](topTracksString)("Your Top Tracks", TrackList.convertToStringSeq)
      }
    }

  def cacheRecommendedTracks(topTracks: TrackList)(implicit accessToken: AccessToken): Result = {

    val token        = accessToken.access_token
//    val topTracksRaw = getTopTracks(token)
//
//    val error: Either[circe.Error, Error] = decode[Error](topTracksRaw)
//
//    val topTracksDecoded: Either[circe.Error, TrackList] = decode[TrackList](topTracksRaw)

    val seedTracks: Seq[Track] = topTracks.items.take(5)

      val seedTrackIds = seedTracks.map(_.id)
      val params = Map(
        "limit"       -> "10", // number of recommendations to return
        "seed_tracks" -> seedTrackIds.mkString(",")
      )
      val joinedParams                       = joinURLParameters(params)
      val endpoint                           = s"$recommendationsEndpoint?$joinedParams"
      val responseFuture: Future[WSResponse] = hitApi(endpoint, token).get()
      val recommendationsJson: String = Await.result(responseFuture, Duration.Inf).body


    val recommendations: Either[circe.Error, Recommendations] = decode[Recommendations](recommendationsJson)

    recommendations match {
      case Left(decodingError) => InternalServerError(decodingError.getMessage)
      case Right(recommendations) =>
        Await.result(cache.set("recommendedTracks", recommendations), Duration.Inf)
        Ok
    }
  }

  def getRecommendedTracks(): Action[AnyContent] =
    Action { implicit request: Request[AnyContent] =>
      val accessToken: Option[String] = getAccessToken(request)
      accessToken.fold(redirectToAuthorize) { token =>

//        val topTracksRaw = getTopTracks(token)
//
//        val error: Either[circe.Error, Error] = decode[Error](topTracksRaw)
//
//        val topTracksDecoded: Either[circe.Error, TrackList] = decode[TrackList](topTracksRaw)
//
//        val seedTracksOrError: Either[circe.Error, Seq[Track]] = topTracksDecoded.map(_.items.take(5))
//
//        val recommendationsJson: Either[circe.Error, String] = seedTracksOrError.map { seedTracks =>
//          val seedTrackIds = seedTracks.map(_.id)
//          val params = Map(
//            "limit"       -> "10", // number of recommendations to return
//            "seed_tracks" -> seedTrackIds.mkString(",")
//          )
//          val joinedParams                       = joinURLParameters(params)
//          val endpoint                           = s"$recommendationsEndpoint?$joinedParams"
//          val responseFuture: Future[WSResponse] = hitApi(endpoint, token).get()
//          Await.result(responseFuture, Duration.Inf).body
//        }
//
//        val recommendations: Either[circe.Error, Recommendations] = recommendationsJson.flatMap(decode[Recommendations])

        val topTracks: Option[TrackList] = Await.result(cache.get[TrackList]("topTracks"), Duration.Inf)
        val recommendedTracks: Option[Recommendations] = Await.result(cache.get[Recommendations]("recommendedTracks"), Duration.Inf)

        (topTracks, recommendedTracks) match {
//          case (Right(Error(ErrorDetails(401, _))), _, _)     => redirectToAuthorize
//          case (Right(Error(ErrorDetails(_, message))), _, _) => InternalServerError(message)
//          case (_, Right(recommendations), Right(seedTracks)) =>
//            Ok(views.html.recommendations(seedTracks, recommendations.tracks))
//          case _ => InternalServerError("Response couldn't be decoded as an error or artist details...")
          case (Some(tracks), Some(recommendations)) =>Ok(views.html.recommendations(tracks.items, recommendations.tracks))
          case _ => InternalServerError("ug oh ")
        }
      }
    }

  def saveTrack(trackId: String): Action[AnyContent] =
    Action { implicit request =>
      println(trackId)
      Continue
    }

}
