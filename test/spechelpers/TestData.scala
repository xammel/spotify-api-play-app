package spechelpers

import io.circe.generic.auto._
import io.circe.syntax._
import models._
import utils.CacheMethods.UNAUTHORIZED
trait TestData {

  lazy val image = Image(height = 1, url = "...", width = 1)
  lazy val track: Track =
    Track(id = "id1", name = "song", artists = Seq(ArtistLite("artist")), album = Album(images = Seq(image)))

  lazy val trackList     = TrackList(items = Seq(track))
  lazy val trackListJson = trackList.asJson.noSpaces

  lazy val recommendations = Recommendations(
    seeds = Seq(RecommendationSeed(afterFilteringSize = 1, afterRelinkingSize = 1, id = track.id, initialPoolSize = 1)),
    tracks = Seq(track)
  )
  lazy val recommendationsJson = recommendations.asJson.noSpaces

  case class ErrorRaw(error: ErrorDetails)
  lazy val unauthorizedSpotifyError     = ErrorRaw(error = ErrorDetails(UNAUTHORIZED, "hi"))
  lazy val unauthorizedSpotifyErrorJson = unauthorizedSpotifyError.asJson.noSpaces

  case class UnexpectedResponse(payload: String)
  lazy val unexpectedResponse     = UnexpectedResponse(payload = "Unexpected Response")
  lazy val unexpectedResponseJson = unexpectedResponse.asJson.noSpaces

  case class AccessTokenFromSpotify(access_token: String)
  lazy val testAccessTokenString = "abc123"
  lazy val accessToken           = AccessTokenFromSpotify(testAccessTokenString)
  lazy val accessTokenJsonString = accessToken.asJson.noSpaces

  lazy val artistList: ArtistList = ArtistList(Seq(Artist("artist", Seq(image))))
  lazy val artistListJson         = artistList.asJson.noSpaces

}
