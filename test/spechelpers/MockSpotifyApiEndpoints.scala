package spechelpers

import mockws.MockWS
import mockws.MockWSHelpers.Action
import play.api.http.Status
import play.api.mvc._
import play.api.test.Helpers._
import utils.StringConstants.{apiTokenEndpoint, myTopArtistsEndpoint, myTopTracksEndpointWithParams, recommendationsEndpointWithParams}

trait MockSpotifyApiEndpoints extends Results with TestData with Status {

  val recommendationsEndpoint = recommendationsEndpointWithParams(Seq(track.id))

  def mockWS(
      accessTokenIsExpired: Boolean = false,
      returnUnexpectedResponse: Boolean = false,
      returnNonAuthError: Boolean = false
  ): MockWS =
    MockWS {
      case _ if returnUnexpectedResponse          => Action { Ok(unexpectedResponseJson) }
      case _ if accessTokenIsExpired              => Action { Ok(unauthorizedSpotifyErrorJson) }
      case _ if returnNonAuthError                => Action { Ok(nonAuthSpotifyErrorJson) }
      case (GET, `myTopTracksEndpointWithParams`) => Action { Ok(trackListJson) }
      case (GET, `recommendationsEndpoint`)       => Action { Ok(recommendationsJson) }
      case (POST, `apiTokenEndpoint`)             => Action { Ok(accessTokenJsonString) }
      case (GET, `myTopArtistsEndpoint`)          => Action { Ok(artistListJson) }
    }

}
