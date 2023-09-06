// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/maxmelhuish/repos/SpotifyApiProject/PlayFrameworkSpotifyApi/play-samples-play-scala-hello-world-tutorial/conf/routes
// @DATE:Thu Sep 07 00:17:42 BST 2023

package controllers;

import router.RoutesPrefix;

public class routes {
  
  public static final controllers.ReverseHomeController HomeController = new controllers.ReverseHomeController(RoutesPrefix.byNamePrefix());
  public static final controllers.ReverseWSController WSController = new controllers.ReverseWSController(RoutesPrefix.byNamePrefix());
  public static final controllers.ReverseAssets Assets = new controllers.ReverseAssets(RoutesPrefix.byNamePrefix());

  public static class javascript {
    
    public static final controllers.javascript.ReverseHomeController HomeController = new controllers.javascript.ReverseHomeController(RoutesPrefix.byNamePrefix());
    public static final controllers.javascript.ReverseWSController WSController = new controllers.javascript.ReverseWSController(RoutesPrefix.byNamePrefix());
    public static final controllers.javascript.ReverseAssets Assets = new controllers.javascript.ReverseAssets(RoutesPrefix.byNamePrefix());
  }

}
