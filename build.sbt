name := """scala-gitlab-api"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  jdbc,
  ehcache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,
  "com.github.tototoshi" %% "play-json4s-native" % "0.8.0",
  "com.github.tototoshi" %% "play-ws-standalone-json4s-native" % "0.1.0",
  "com.github.tototoshi" %% "play-json4s-test-native" % "0.8.0" % Test
)
