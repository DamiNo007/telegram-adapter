name := "TelegramAdapter"

version := "0.0.1"

scalaVersion := "2.12.11"

libraryDependencies ++= Seq(
  "com.bot4s" %% "telegram-core" % "4.4.0-RC2",
  "com.softwaremill.sttp" %% "core" % "1.6.4",
  "com.typesafe.akka" %% "akka-actor" % "2.6.7",
  "com.typesafe.akka" %% "akka-http" % "10.1.12",
  "com.lihaoyi" %% "requests" % "0.5.1",
  "com.typesafe.akka" %% "akka-stream" % "2.6.7",
  "org.json4s" %% "json4s-native" % "3.6.6",
  "org.json4s" %% "json4s-jackson" % "3.6.6"
)