package controllers

import io.circe
import javax.inject.Inject
import play.api.libs.ws._
import play.api.mvc._
import utils.StringConstants.{getArtistEndpoint, myTopArtistsEndpoint, searchApi, myTopTracksEndpoint}

import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{Await, Future}
import utils.Functions.{getAccessToken, getAccessTokenUnsafe, redirectToAuthorize}
import io.circe.parser._
import io.circe._
import models.{Artist, ArtistList, Error, ErrorDetails, TrackList}

class ApiCallController @Inject() (
    ws: WSClient,
    val controllerComponents: ControllerComponents
) extends BaseController {

  def hitApi(url: String, token: String): WSRequest =
    ws.url(url)
      .addHttpHeaders("Authorization" -> s"Bearer $token")
      .withRequestTimeout(10000.millis)

  def processResponse[T: Manifest](responseBody: String)(dataToString: T => String)(implicit decoder: Decoder[T]) = {
    val error: Either[circe.Error, Error] = decode[Error](responseBody)
    val data: Either[circe.Error, T]      = decode[T](responseBody)
    (error, data) match {
      case (Right(Error(ErrorDetails(401, _))), _)     => redirectToAuthorize
      case (Right(Error(ErrorDetails(_, message))), _) => InternalServerError(message)
      case (_, Right(data: T))                         => Ok(views.html.showArtist(dataToString(data)))
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

        println(searchURL(""))

        val response = Await.result(searchResponse(""), Duration.Inf)

        Ok(views.html.search(response.body))
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

        //todo Make this nicer so we don't extract the value from the Future
        val response: WSResponse = Await.result(artistResponse(artistId), Duration.Inf)

        processResponse[Artist](response.body)(Artist.convertToString)
      }
    }

  def getMyTopArtists(): Action[AnyContent] =
    Action { implicit request: Request[AnyContent] =>
      val accessToken: Option[String] = getAccessToken(request)
      accessToken.fold(redirectToAuthorize) { token =>
        val responseFuture: Future[WSResponse] = hitApi(myTopArtistsEndpoint, token).get()
        val response: WSResponse               = Await.result(responseFuture, Duration.Inf)
        processResponse[ArtistList](response.body)(ArtistList.convertToString)
      }
    }

  def getMyTopTracks(): Action[AnyContent] =
    Action { implicit request: Request[AnyContent] =>
      val accessToken: Option[String] = getAccessToken(request)
      accessToken.fold(redirectToAuthorize) { token =>
        val responseFuture: Future[WSResponse] = hitApi(myTopTracksEndpoint, token).get()
        val response: WSResponse               = Await.result(responseFuture, Duration.Inf)
        processResponse[TrackList](response.body)(TrackList.convertToString)
      }
    }
}
