name := "Agora-REST-API"
 
version := "1.0" 

lazy val `Agora-REST-API` = (project in file(".")).enablePlugins(PlayScala)
scalacOptions ++= Seq("-deprecation", "-language:_")

scalaVersion := "2.12.3"
val reactiveMongoVersion = "0.13.0-play26"
val silhouetteVersion = "5.0.3"
val playMailerVersion = "6.0.1"
val playJsonVersion = "2.6.9"
val swaggerUIVersion = "3.6.1"

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "play2-reactivemongo" % reactiveMongoVersion,
  "com.mohiva" %% "play-silhouette" % silhouetteVersion,
  "com.mohiva" %% "play-silhouette-persistence" % silhouetteVersion,
  "com.mohiva" %% "play-silhouette-password-bcrypt" % silhouetteVersion,
  "com.mohiva" %% "play-silhouette-crypto-jca" % silhouetteVersion,
  "com.mohiva" %% "play-silhouette-testkit" % silhouetteVersion % "test",
  "com.iheart" %% "ficus" % "1.4.3",
  "com.typesafe.play" %% "play-mailer" % playMailerVersion,
  "com.typesafe.play" %% "play-mailer-guice" % playMailerVersion,
  "com.enragedginger" %% "akka-quartz-scheduler" % "1.6.1-akka-2.5.x",
  "net.codingwell" %% "scala-guice" % "4.1.1",
  "com.adrianhurt" %% "play-bootstrap" % "1.2-P26-B3",
  "com.typesafe.play" %% "play-json" % playJsonVersion,
  "org.typelevel" %% "spire" % "0.14.1",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
  "com.typesafe.play" %% "play-json-joda" % playJsonVersion,
  "io.swagger" %% "swagger-play2" % "1.6.0",
  "org.webjars" % "swagger-ui" % swaggerUIVersion,
  specs2 % Test,
  ehcache,
  guice
)

unmanagedResourceDirectories in Test += (baseDirectory.value / "target/web/public/test")

resolvers += Resolver.jcenterRepo
resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
resolvers += "iheartradio-maven" at "https://dl.bintray.com/iheartradio/maven"
resolvers += "atlassian-maven" at "https://maven.atlassian.com/content/repositories/atlassian-public"