package spechelpers

import mockws.MockWS
import mockws.MockWSHelpers.Action
import play.api.mvc._
import play.api.test.Helpers._
import spechelpers.TestData.trackListJson
import utils.ApiMethods.joinURLParameters
import utils.StringConstants.{myTopTracksEndpointWithParams, topTracksParams}

trait MockSpotifyApiEndpoints extends Results {

  val ws: MockWS = MockWS {
    case (GET, `myTopTracksEndpointWithParams`) => Action { Ok(trackListJson) }
    case (GET, ) => Action { Ok() }
  }

}
