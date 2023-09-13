package controllers

import io.circe
import javax.inject.Inject
import play.api.libs.ws._
import play.api.mvc._
import utils.StringConstants.{getArtistEndpoint, searchApi}

import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{Await, Future}
import utils.Functions.getAccessToken
import io.circe.parser._
import models.{Error, ArtistDetails, ErrorDetails}

class WSController @Inject()(ws: WSClient,
                             val controllerComponents: ControllerComponents)
    extends BaseController {

  def hitApi(url: String, token: String) =
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
        def searchResponse(query: String) = searchRequest("").get()

        println(searchURL(""))

        val response = Await.result(searchResponse(""), Duration.Inf)

        Ok(views.html.search(response.body))
      }
    }

  def getArtist(artistId: String): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      {

        val accessToken: String = getAccessToken(request)

        def getArtistURL(artistId: String) = s"$getArtistEndpoint/$artistId"
        def artistRequest(artistId: String): WSRequest =
          hitApi(getArtistURL(artistId), accessToken)
        def artistResponse(artistId: String): Future[WSResponse] =
          artistRequest(artistId).get()

        //todo Make this nicer so we don't extract the value from the Future
        val response: WSResponse =
          Await.result(artistResponse(artistId), Duration.Inf)

        val error = decode[Error](response.body)
        val artistDetails: Either[circe.Error, ArtistDetails] =
          decode[ArtistDetails](response.body)

        (error, artistDetails) match {
          case (Left(e), Right(v)) => Ok(views.html.showArtist(v.name))
          case (Right(Error(ErrorDetails(401, _))), Left(e)) =>
            Redirect(routes.AuthorizationController.authorize())
          case (Right(v), Left(e)) =>
            Ok(
              views.html.showArtist(
                s"Error! Error code: ${v.error.status} Error Message: ${v.error.message}"
              )
            )
          case _ =>
            InternalServerError(
              "Response couldn't be decoded as an error or artist details..."
            )
        }
      }
  }
}
