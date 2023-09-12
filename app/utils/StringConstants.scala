package utils

object StringConstants {

  //URLs
  val https = "https:/"
  val apiSpotify = "api.spotify.com/v1"
  val getArtistEndpoint = s"$https/$apiSpotify/artists"
  val searchEndpoint = s"$https/$apiSpotify/search"
  val authorizeEndpoint = s"$https/accounts.spotify.com/authorize?"
  def searchApi(query: String) = s"$searchEndpoint?q=$query" // remaster%2520track%3ADoxy%2520artist%3AMies%2520Davis&type=album

  //creds
  //todo change this so it's not stored here
  lazy val currentToken = "BQDw-NyNBcvkYNNYUdqqkwhwyAp1jLSqvHkxhQslIhsckdFQ45oNjrxY8e3wstPD1QdeZdCNC3I9kORe3-rEUyt9JnLOXbWifZMSze1rBn_GqcAPvUE"
  val CLIENT_ID="84209618a4864d94a1dfefe1cbe5a309"
  //val CLIENT_SECRET="091acdc2050f432aaa4cd7170e73b74d"
}
