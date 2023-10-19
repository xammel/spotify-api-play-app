package utils

import play.api.libs.json.{JsObject, Json}
import utils.ApiMethods.joinURLParameters
import utils.AuthorizationMethods.generateCodeChallenge

object StringConstants {

  // URL beginnings
  val https = "https:/"
  val http  = "http:/"

  // Spotify URLs
  val apiSpotify              = "api.spotify.com/v1"
  val accountsSpotify         = "accounts.spotify.com"
  val authorizeEndpoint       = s"$https/$accountsSpotify/authorize?"
  val apiTokenEndpoint        = s"$https/$accountsSpotify/api/token"
  val meTopEndpoint           = s"$https/$apiSpotify/me/top"
  val myTopArtistsEndpoint    = s"$meTopEndpoint/artists"
  val myTopTracksEndpoint     = s"$meTopEndpoint/tracks"
  val recommendationsEndpoint = s"$https/$apiSpotify/recommendations"
  val myTracksEndpoint        = s"$https/$apiSpotify/me/tracks"

  // Fully Qualified API Endpoints

  val myTopTracksEndpointWithParams = s"$myTopTracksEndpoint?${joinURLParameters(topTracksParams)}"
  def recommendationsEndpointWithParams(topFiveTrackIds: Seq[String]) =
    s"$recommendationsEndpoint?${joinURLParameters(recommendationsParams(topFiveTrackIds))}"

  def authorizationEndpointWithParams(codeVerifier: String) = {
    val codeChallenge = generateCodeChallenge(codeVerifier)
    val params        = authorizeParams(codeChallenge)
    s"$authorizeEndpoint${joinURLParameters(params)}"
  }

  def apiTokenPayload(code: String, codeVerifier: String) = joinURLParameters(callbackParams(code, codeVerifier))

  // Local URLs
  val localhost             = "localhost:9000"
  val authorizationCallback = s"$http/$localhost/authorization-callback"

  // Credentials
  val clientId = "84209618a4864d94a1dfefe1cbe5a309"

  // Constants
  val lengthOfCodeVerifier = 128
  val sha256               = "SHA-256"

  // Cache keys
  val tokenKey                  = "token"
  val topTracksCacheKey         = "topTracks"
  val recommendedTracksCacheKey = "recommendedTracks"

  // Http Request Parameters
  lazy val topTracksParams = Map(
    "time_range" -> "short_term", // short_term = last 4 weeks, medium_term = last 6 months, long_term = all time
    "limit"      -> "20" // Number of tracks to return
  )

  def callbackParams(code: String, codeVerifier: String) =
    Map(
      "grant_type"    -> "authorization_code",
      "code"          -> code,
      "redirect_uri"  -> authorizationCallback,
      "client_id"     -> clientId,
      "code_verifier" -> codeVerifier
    )

  val requiredPermissions = Seq(
    "user-read-private",
    "user-read-email",
    "user-top-read",
    "user-library-modify"
  )

  def authorizeParams(codeChallenge: String) =
    Map(
      "response_type"         -> "code",
      "client_id"             -> clientId,
      "scope"                 -> requiredPermissions.mkString(" "),
      "redirect_uri"          -> authorizationCallback,
      "code_challenge_method" -> "S256",
      "code_challenge"        -> codeChallenge
    )

  def recommendationsParams(seedTrackIds: Seq[String]) =
    Map(
      "limit"       -> "10", // number of recommendations to return
      "seed_tracks" -> seedTrackIds.mkString(",")
    )

  def trackIdJson(id: String): JsObject = Json.obj("ids" -> Seq(id.trim))

}
