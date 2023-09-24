//Dependency versions
val circeVersion = "0.14.3"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := """spotify-api-app""",
    organization := "com.example",
    version := "1.0-SNAPSHOT",
    scalaVersion := "2.13.4",
    libraryDependencies ++= Seq(
      guice,
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test,
      ws,
//      ehcache,
//      cacheApi,
      caffeine,
    ) ++ Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % circeVersion),
    scalacOptions ++= Seq("-feature", "-deprecation", "-Xfatal-warnings"),
    routesImport += "models.Binders"
  )
