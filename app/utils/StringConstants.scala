package utils

import Functions.joinURLParameters

object StringConstants {

  //URLs
  val https = "https:/"
  val http = "http:/"

  //Spotify URLs
  val apiSpotify = "api.spotify.com/v1"
  val accountsSpotify = "accounts.spotify.com"
  val getArtistEndpoint = s"$https/$apiSpotify/artists"
  val searchEndpoint = s"$https/$apiSpotify/search"
  val authorizeEndpoint = s"$https/$accountsSpotify/authorize?"
  val apiTokenEndpoint = s"$https/$accountsSpotify/api/token"
  def searchApi(query: String) =
    s"$searchEndpoint?q=$query" // remaster%2520track%3ADoxy%2520artist%3AMies%2520Davis&type=album
  val meTopEndpoint = s"$https/$apiSpotify/me/top"
  val myTopArtistsEndpoint = s"$meTopEndpoint/artists"
  val myTopTracksEndpoint = s"$meTopEndpoint/tracks?time_range=short_term"

  //Local URLs
  val localhost = "localhost:9000"
  val authorizationCallback = s"$http/$localhost/authorization-callback"

  //creds
  val clientId = "84209618a4864d94a1dfefe1cbe5a309"

  // constants
  val lengthOfCodeVerifier = 128
  val sha256 = "SHA-256"
  val tokenKey = "token"
}
