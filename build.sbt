name := """agora-web"""
organization := "com.aossie"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.8"

resolvers += Resolver.jcenterRepo
resolvers += "Sonatype OSS Snapshots".at("https://oss.sonatype.org/content/repositories/snapshots/")

libraryDependencies ++= Seq(
  "com.mohiva"             %% "play-silhouette" % "4.0.0",
  "com.mohiva"             %% "play-silhouette-password-bcrypt" % "4.0.0",
  "com.mohiva"             %% "play-silhouette-persistence" % "4.0.0",
  "com.mohiva"             %% "play-silhouette-crypto-jca" % "4.0.0",
  "org.webjars"            %% "webjars-play" % "2.5.0-2",
  "net.codingwell"         %% "scala-guice" % "4.0.1",
  "com.iheart"             %% "ficus" % "1.2.6",
  "org.webjars"            % "bootstrap-datepicker" % "1.6.4",
  "org.webjars"            % "font-awesome" % "4.7.0",
  "com.enragedginger"      %% "akka-quartz-scheduler" % "1.5.0-akka-2.4.x",
  "com.adrianhurt"         %% "play-bootstrap" % "1.1.1-P25-B3",
  "com.mohiva"             %% "play-silhouette-testkit" % "4.0.0" % "test",
  "org.webjars"            % "bootstrap-tagsinput" % "0.6.1",
  "org.webjars"            % "jquery-ui" % "1.12.1",
  "com.novus"              %% "salat" % "1.9.9",
  "com.typesafe.play"      %% "play-mailer-guice" % "6.0.0",
  "org.quartz-scheduler"   % "quartz" % "2.2.2",
  "org.mongodb"            %% "casbah" % "3.1.1",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  specs2              % Test,
  cache,
  filters
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)

routesGenerator := InjectedRoutesGenerator

routesImport += "utils.route.Binders._"

scalacOptions ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xfatal-warnings", // Fail the compilation if there are any warnings.
  "-Xlint", // Enable recommended additional warnings.
  "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver.
  "-Ywarn-dead-code", // Warn when dead code is identified.
  "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
  "-Ywarn-nullary-override", // Warn when non-nullary overrides nullary, e.g. def foo() over def foo.
  "-Ywarn-numeric-widen" // Warn when numerics are widened.
)
