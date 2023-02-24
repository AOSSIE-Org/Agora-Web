name := "Agora-REST-API"
 
version := "1.0" 

lazy val `Agora-REST-API` = (project in file(".")).enablePlugins(PlayScala)
scalacOptions ++= Seq("-deprecation", "-language:_")

scalaVersion := "2.12.17"
val reactiveMongoVersion = "0.20.10-play27"
val silhouetteVersion = "6.1.1"
val playMailerVersion = "7.0.1"
val playJsonVersion = "2.7.4"
val swaggerPlay2Version = "1.7.1"
val swaggerUIVersion = "3.25.2"

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "play2-reactivemongo" % reactiveMongoVersion,
  "com.mohiva" %% "play-silhouette" % silhouetteVersion,
  "com.mohiva" %% "play-silhouette-persistence" % silhouetteVersion,
  "com.mohiva" %% "play-silhouette-password-bcrypt" % silhouetteVersion,
  "com.mohiva" %% "play-silhouette-crypto-jca" % silhouetteVersion,
  "com.mohiva" %% "play-silhouette-testkit" % silhouetteVersion % "test",
  "com.iheart" %% "ficus" % "1.4.7",
  "com.typesafe.play" %% "play-mailer" % playMailerVersion,
  "com.typesafe.play" %% "play-mailer-guice" % playMailerVersion,
  "com.enragedginger" %% "akka-quartz-scheduler" % "1.6.1-akka-2.5.x",
  "net.codingwell" %% "scala-guice" % "4.2.6",
  "com.adrianhurt" %% "play-bootstrap" % "1.6.1-P27-B4",
  "com.typesafe.play" %% "play-json" % playJsonVersion,
  "org.typelevel" %% "spire" % "0.14.1",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "com.typesafe.play" %% "play-json-joda" % playJsonVersion,
  "io.swagger" %% "swagger-play2" % swaggerPlay2Version,
  "org.webjars" % "swagger-ui" % swaggerUIVersion,
  specs2 % Test,
  ehcache,
  guice
)

mainClass in assembly := Some("play.core.server.ProdServerStart")

assemblyJarName in assembly := "agora-api.jar"

assemblyOutputPath in assembly := new File(s"dist/${(assembly/assemblyJarName).value}")

fullClasspath in assembly += Attributed.blank(PlayKeys.playPackageAssets.value)

assemblyMergeStrategy in assembly := {
  case manifest if manifest.contains("MANIFEST.MF") =>
    // We don't need manifest files since sbt-assembly will create
    // one with the given settings
    MergeStrategy.discard
  case referenceOverrides if referenceOverrides.contains("reference-overrides.conf") || referenceOverrides.contains("reference.conf") =>
    // Keep the content for all reference-overrides.conf files
    MergeStrategy.concat
  case PathList("META-INF", ps@_*) =>
    if (ps.map(_.toLowerCase).exists(a => a.contains("swagger-ui")))
      MergeStrategy.singleOrError
    else MergeStrategy.discard
  case x if x.endsWith("module-info.class") => MergeStrategy.discard
  case _ => MergeStrategy.first
}

unmanagedResourceDirectories in Test += (baseDirectory.value / "target/web/public/test")

resolvers += Resolver.jcenterRepo
resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
resolvers += "iheartradio-maven" at "https://dl.bintray.com/iheartradio/maven"
resolvers += "atlassian-maven" at "https://maven.atlassian.com/content/repositories/atlassian-public"