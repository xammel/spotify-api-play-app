package controllers

import javax.inject.Inject
import play.api.libs.ws._
import play.api.mvc._
import utils.StringConstants.{getArtistEndpoint, searchApi}

import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{Await, Future}
import utils.Functions.getAccessToken

class WSController @Inject()(ws: WSClient,
                             val controllerComponents: ControllerComponents)
    extends BaseController {

  def hitApi(url: String, token: String) = ws.url(url)
    .addHttpHeaders("Authorization" -> s"Bearer $token")
    .withRequestTimeout(10000.millis)

  def findArtist(accessToken: String, artist: String): Action[AnyContent] = Action { implicit request: Request[AnyContent] => {
    def searchURL(query: String) = s"${searchApi("Miles Davis&type=artist")}" //"remaster%2520track%3ADoxy%2520artist%3AMies%2520Davis&type=album")}"
    def searchRequest(query: String) = hitApi(searchURL(""), accessToken) //todo remove hardcoding
    def searchResponse(query: String) = searchRequest("").get()

    println(searchURL(""))

    val response = Await.result(searchResponse(""), Duration.Inf)

    Ok(views.html.search(response.body))
  }}

  def getArtist(artistId: String): Action[AnyContent] = Action { implicit request: Request[AnyContent] => {

    val accessToken: String = getAccessToken(request)

    def getArtistURL(artistId: String) = s"$getArtistEndpoint/$artistId"
    def artistRequest(artistId: String): WSRequest = hitApi(getArtistURL(artistId), accessToken)
    def artistResponse(artistId: String): Future[WSResponse] = artistRequest(artistId).get()

    //todo Make this nicer so we don't extract the value from the Future
    val response: WSResponse = Await.result(artistResponse(artistId), Duration.Inf)

    Ok(views.html.showArtist(response.body))
  }
  }
}
