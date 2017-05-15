name := """agora-web"""
organization := "com.aossie"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.11"

libraryDependencies += filters
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test
libraryDependencies += "org.webjars" %% "webjars-play" % "2.5.0"
libraryDependencies += "org.webjars" % "bootstrap" % "3.1.1-2"
libraryDependencies += "org.webjars" % "bootstrap-datepicker" % "1.6.4"
libraryDependencies += "org.webjars" % "font-awesome" % "4.7.0"
libraryDependencies += "org.webjars" % "foundation" % "6.3.1"


// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.aossie.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.aossie.binders._"
