package utils

object StringConstants {

  //URLs
  val https = "https:/"
  val apiSpotify = "api.spotify.com/v1"
  val getArtistEndpoint = s"$https/$apiSpotify/artists"
  val searchEndpoint = s"$https/$apiSpotify/search"
  def searchApi(query: String) = s"$searchEndpoint?q=$query" // remaster%2520track%3ADoxy%2520artist%3AMies%2520Davis&type=album

  //creds
  //todo change this so it's not stored here
  lazy val currentToken = "BQAWbIEVtnD47a-qNmOqpNNxkqlKCLByrlZGT_6icG-kMQjO9-vhrml3qJt62MMP8dBhCIolEne6WK1T6v3wbSmOl9Fn3rMkPDwj0MzNTmxSVs_Mh0U"

}
