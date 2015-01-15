name := """scala-gitlab-api"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.4"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  "org.scalatestplus" %% "play" % "1.2.0" % "test",
  "com.github.tototoshi" %% "play-json4s-native" % "0.3.0",
  "com.github.tototoshi" %% "play-json4s-test-native" % "0.3.0" % "test"
)
