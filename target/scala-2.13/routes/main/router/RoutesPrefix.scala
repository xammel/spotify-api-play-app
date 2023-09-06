// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/maxmelhuish/repos/SpotifyApiProject/PlayFrameworkSpotifyApi/play-samples-play-scala-hello-world-tutorial/conf/routes
// @DATE:Thu Sep 07 00:17:42 BST 2023


package router {
  object RoutesPrefix {
    private var _prefix: String = "/"
    def setPrefix(p: String): Unit = {
      _prefix = p
    }
    def prefix: String = _prefix
    val byNamePrefix: Function0[String] = { () => prefix }
  }
}
