name := """agora-web"""
organization := "com.aossie"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.11"


resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"


libraryDependencies += filters
libraryDependencies ++= Seq(
 "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test,
 "org.webjars" %% "webjars-play" % "2.5.0",
 "org.webjars" % "bootstrap" % "3.1.1-2",
 "org.webjars" % "bootstrap-datepicker" % "1.6.4",
 "com.adrianhurt" %% "play-bootstrap" % "1.1.1-P25-B3-SNAPSHOT",
 "org.webjars" % "font-awesome" % "4.7.0",
 "org.webjars" % "foundation" % "6.3.1")


// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.aossie.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.aossie.binders._"

routesGenerator := InjectedRoutesGenerator