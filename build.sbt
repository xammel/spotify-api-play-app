//Dependency versions
lazy val circeVersion = "0.14.3"
lazy val circeDependencies = Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := """spotify-api-play-app""",
    organization := "com.xammel",
    version := "1.0",
    scalaVersion := "2.13.4",
    libraryDependencies ++= Seq(
      guice,
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test,
      ws,
      caffeine,
    ) ++ circeDependencies,
    scalacOptions ++= Seq("-feature", "-deprecation", "-Xfatal-warnings")
  )
