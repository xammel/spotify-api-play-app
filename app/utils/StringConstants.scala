package utils

object StringConstants {

  // URL beginnings
  val https = "https:/"
  val http = "http:/"

  // Spotify URLs
  val apiSpotify = "api.spotify.com/v1"
  val accountsSpotify = "accounts.spotify.com"
  val authorizeEndpoint = s"$https/$accountsSpotify/authorize?"
  val apiTokenEndpoint = s"$https/$accountsSpotify/api/token"
  val meTopEndpoint = s"$https/$apiSpotify/me/top"
  val myTopArtistsEndpoint = s"$meTopEndpoint/artists"
  val myTopTracksEndpoint = s"$meTopEndpoint/tracks"
  val recommendationsEndpoint = s"$https/$apiSpotify/recommendations"
  val myTracksEndpoint = s"$https/$apiSpotify/me/tracks"

  // Local URLs
  val localhost = "localhost:9000"
  val authorizationCallback = s"$http/$localhost/authorization-callback"

  // Credentials
  val clientId = "84209618a4864d94a1dfefe1cbe5a309"

  // Constants
  val lengthOfCodeVerifier = 128
  val sha256 = "SHA-256"
  val tokenKey = "token"

  // Cache keys
  val topTracksCacheKey = "topTracks"
  val recommendedTracksCacheKey = "recommendedTracks"
}
