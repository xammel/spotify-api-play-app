package controllers

import javax.inject._
import models.{AccessToken, ArtistId, Recommendations, TrackList}
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.mvc._
import utils.Functions.getAccessToken
import play.api.cache.AsyncCacheApi
import utils.Functions.redirectToAuthorize
import play.api.data.Form
import play.api.libs.ws.WSClient
import utils.Functions.{cacheRecommendedTracks, cacheTopTracks}
import utils.StringConstants.tokenKey
import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject() (cache: AsyncCacheApi, ws: WSClient, cc: ControllerComponents)
    extends AbstractController(cc)
    with I18nSupport {

  implicit val implicitCache: AsyncCacheApi = cache
  implicit val wsClient: WSClient = ws

  def index() =
    Action { implicit request: Request[AnyContent] =>
      val accessTokenOpt = getAccessToken(request)
      accessTokenOpt match {
        case Some(_) => Redirect(routes.HomeController.home())
        case None    => Redirect(routes.AuthorizationController.authorize())
      }
    }

  def home(artistIdForm: Form[ArtistId] = ArtistId.artistIdForm.bind(Map("artistId" -> ""))): Action[AnyContent] =
    Action { implicit request: RequestHeader =>

    //TODO remove
    request.session.-(tokenKey)

      val accessTokenOpt: Option[String] = getAccessToken

      println("access token thing", accessTokenOpt)
      accessTokenOpt match {
        case None => redirectToAuthorize
        case Some(token) =>
          implicit val accessToken: AccessToken = AccessToken(token)
          cacheTopTracks
          val topTracks: Option[TrackList] = Await.result(cache.get[TrackList]("topTracks"), Duration.Inf)
          println("inside home 2", topTracks)
          topTracks match {
            case None         => InternalServerError("No top tracks were found in the cache")
            case Some(tracks) => cacheRecommendedTracks(tracks)
          }
      }

      val topTracks: Option[TrackList] = Await.result(cache.get[TrackList]("topTracks"), Duration.Inf)
      val recommendedTracks: Option[Recommendations] = Await.result(cache.get[Recommendations]("recommendedTracks"), Duration.Inf)

      println("inside home")
      println(topTracks, recommendedTracks)

      Ok(views.html.home(artistIdForm))
    }

  def getRecommendedTracks(): Action[AnyContent] =
    Action { implicit request: Request[AnyContent] =>
      val accessToken: Option[String] = getAccessToken(request)
      accessToken.fold(redirectToAuthorize) { token =>
        val topTracks: Option[TrackList] = Await.result(cache.get[TrackList]("topTracks"), Duration.Inf)
        val recommendedTracks: Option[Recommendations] = Await.result(cache.get[Recommendations]("recommendedTracks"), Duration.Inf)

        println("inside reccs")
        println(topTracks, recommendedTracks)

        (topTracks, recommendedTracks) match {
          case (Some(tracks), Some(recommendations)) =>Ok(views.html.recommendations(tracks.items, recommendations.tracks))
          case _ => InternalServerError("ug oh ")
        }
      }
    }

}
