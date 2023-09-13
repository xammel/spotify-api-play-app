package controllers

import io.circe
import javax.inject.Inject
import play.api.libs.ws._
import play.api.mvc._
import utils.StringConstants.{getArtistEndpoint, myTopArtistsEndpoint, searchApi}

import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{Await, Future}
import utils.Functions.{getAccessToken, getAccessTokenUnsafe}
import io.circe.parser._
import models.{ArtistDetails, ArtistList, Error, ErrorDetails}

class ApiCallController @Inject()(
    ws: WSClient,
    val controllerComponents: ControllerComponents
) extends BaseController {

  def hitApi(url: String, token: String): WSRequest =
    ws.url(url)
      .addHttpHeaders("Authorization" -> s"Bearer $token")
      .withRequestTimeout(10000.millis)

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

  def getArtist(artistId: String): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    {

      val accessToken: String = getAccessTokenUnsafe(request)

      def getArtistURL(artistId: String) = s"$getArtistEndpoint/$artistId"
      def artistRequest(artistId: String): WSRequest =
        hitApi(getArtistURL(artistId), accessToken)
      def artistResponse(artistId: String): Future[WSResponse] =
        artistRequest(artistId).get()

      //todo Make this nicer so we don't extract the value from the Future
      val response: WSResponse =
        Await.result(artistResponse(artistId), Duration.Inf)

      val error: Either[circe.Error, Error] = decode[Error](response.body)
      val artistDetails: Either[circe.Error, ArtistDetails] = decode[ArtistDetails](response.body)

      (error, artistDetails) match {
        case (Left(_), Right(v))                           => Ok(views.html.showArtist(v.name))
        case (Right(Error(ErrorDetails(401, _))), Left(_)) => Redirect(routes.AuthorizationController.authorize())
        case (Right(v), Left(_)) =>
          Ok(views.html.showArtist(s"Error! Error code: ${v.error.status} Error Message: ${v.error.message}"))
        case _ =>
          InternalServerError("Response couldn't be decoded as an error or artist details...")
      }
    }
  }

  def getMyTopArtists(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    val accessToken: Option[String] = getAccessToken(request)
    val responseFuture: Option[Future[WSResponse]] = accessToken.map(hitApi(myTopArtistsEndpoint, _).get())
    val responseOpt: Option[WSResponse] = responseFuture.map(Await.result(_, Duration.Inf))
    responseOpt match {
      case Some(response) => {
        val error: Either[circe.Error, Error] = decode[Error](response.body)
        val artists: Either[circe.Error, ArtistList] = decode[ArtistList](response.body)
        (error, artists) match {
          case (Right(Error(ErrorDetails(401, _))), _) => Redirect(routes.AuthorizationController.authorize())
          case (_, Right(artists: ArtistList)) =>
            val artistDetailString = artists.items.map(artist => s"Name: ${artist.name}, Popularity: ${artist.popularity}").mkString(" | ")
            Ok(views.html.showArtist(artistDetailString))
          case _ =>  InternalServerError("Response couldn't be decoded as an error or artist details...")

        }
      }
      case None           => Redirect(routes.AuthorizationController.authorize())
    }
  }
}
