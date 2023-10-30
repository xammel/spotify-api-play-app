package utils

import play.api.libs.json.{JsObject, Json}
import utils.ApiMethods.joinURLParameters
import utils.AuthorizationMethods.generateCodeChallenge

object StringConstants {

  // Client ID for Spotify API - Ok to be public - https://community.spotify.com/t5/Spotify-for-Developers/Do-i-have-to-keep-the-Client-ID-secret/td-p/5258016
  private val spotifyClientId = "84209618a4864d94a1dfefe1cbe5a309"

  // URL beginnings
  val https = "https:/"

  // Spotify URLs
  private val apiSpotify          = "api.spotify.com/v1"
  private val accountsSpotify     = "accounts.spotify.com"
  private val authorizeEndpoint   = s"$https/$accountsSpotify/authorize?"
  private val meTopEndpoint       = s"$https/$apiSpotify/me/top"
  private val myTopTracksEndpoint = s"$meTopEndpoint/tracks"
  val apiTokenEndpoint            = s"$https/$accountsSpotify/api/token"
  val myTopArtistsEndpoint        = s"$meTopEndpoint/artists"
  val recommendationsEndpoint     = s"$https/$apiSpotify/recommendations"
  val myTracksEndpoint            = s"$https/$apiSpotify/me/tracks"

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
  private val authorizationCallback = s"http://localhost:9000/authorization-callback"

  // Constants
  val lengthOfCodeVerifier  = 128
  val sha256                = "SHA-256"
  val numberOfTopTrackSeeds = 5

  // Cache keys
  val tokenKey                  = "token"
  val topTracksCacheKey         = "topTracks"
  val recommendedTracksCacheKey = "recommendedTracks"

  // Http Request Parameters
  lazy val topTracksParams = Map(
    "time_range" -> "short_term", // short_term = last 4 weeks, medium_term = last 6 months, long_term = all time
    "limit"      -> "20" // Number of tracks to return
  )

  private def callbackParams(code: String, codeVerifier: String) =
    Map(
      "grant_type"    -> "authorization_code",
      "code"          -> code,
      "redirect_uri"  -> authorizationCallback,
      "client_id"     -> spotifyClientId,
      "code_verifier" -> codeVerifier
    )

  private val requiredPermissions = Seq(
    "user-read-private",
    "user-read-email",
    "user-top-read",
    "user-library-modify"
  )

  private def authorizeParams(codeChallenge: String) =
    Map(
      "response_type"         -> "code",
      "client_id"             -> spotifyClientId,
      "scope"                 -> requiredPermissions.mkString(" "),
      "redirect_uri"          -> authorizationCallback,
      "code_challenge_method" -> "S256",
      "code_challenge"        -> codeChallenge
    )

  private def recommendationsParams(seedTrackIds: Seq[String]) =
    Map(
      "limit"       -> "10", // number of recommendations to return
      "seed_tracks" -> seedTrackIds.mkString(",")
    )

  def trackIdJson(id: String): JsObject = Json.obj("ids" -> Seq(id.trim))

  // Error messages

  def getCacheErrorMessage(key: String)  = s"Could not retrieve item from cache with key: $key"
  def cacheTopTracksErrorMessage         = "Couldn't decode response as a known error or track list"
  def cacheRecommendedTracksErrorMessage = "Couldn't decode response as a known error or recommended tracks"
}
