package controllers

import akka.Done
import io.circe
import io.circe._
import io.circe.parser._
import javax.inject.Inject
import models.{Artist, ArtistList, Error, ErrorDetails, Recommendations, TrackList}
import play.api.cache._
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

  val hitApiWithClient = hitApi(ws)(_, _)

  //TODO set recommended tracks using a cache like this:
  //TODO remove
  val result: Future[Done]         = cache.set("item.key", 2)
  val futureMaybeUser: Option[Int] = Await.result(cache.get[Int]("item.key"), Duration.Inf)
  println("future maybe user", futureMaybeUser)

  def processResponse[T: Manifest](
      responseBody: String
  )(title: String, dataToStringSeq: T => Seq[String])(implicit decoder: Decoder[T]) = {
    val error: Either[circe.Error, Error] = decode[Error](responseBody)
    val data: Either[circe.Error, T]      = decode[T](responseBody)
    (error, data) match {
      case (Right(Error(ErrorDetails(401, _))), _) => {
        //TODO remove
        println("inside processResponse redirect")
        redirectToAuthorize
      }
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
          hitApiWithClient(searchURL(""), accessToken) //todo remove hardcoding
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
          hitApiWithClient(getArtistURL(artistId), accessToken)
        def artistResponse(artistId: String): Future[WSResponse] =
          artistRequest(artistId).get()

        processResponse[Artist]("")("Get Artist", Artist.convertToStringSeq)
      }
    }

  def getMyTopArtists(): Action[AnyContent] =
    Action { implicit request: Request[AnyContent] =>
      val accessToken: Option[String] = getAccessToken(request)
      accessToken.fold(redirectToAuthorize) { token =>
        val responseFuture: Future[WSResponse] = hitApiWithClient(myTopArtistsEndpoint, token).get()
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
    val responseFuture: Future[WSResponse] = hitApiWithClient(endpoint, accessToken).get()
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
      val topTracks: Option[TrackList] = Await.result(cache.get[TrackList]("topTracks"), Duration.Inf)
      val recommendedTracks: Option[Recommendations] =
        Await.result(cache.get[Recommendations]("recommendedTracks"), Duration.Inf)

      //TODO remove
      println("inside reccs")
      println(topTracks, recommendedTracks)

      (topTracks, recommendedTracks) match {
        case (Some(tracks), Some(recommendations)) =>
          Ok(views.html.recommendations(tracks.items, recommendations.tracks))
        case _ => InternalServerError("Could not fetch cached results for top tracks or recommendations")
      }

    }

  def saveTrack(trackId: String): Action[AnyContent] =
    Action { implicit request =>
      //TODO remove
      println(trackId)
      Continue
    }

}
