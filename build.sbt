name := "TelegramAdapter"

version := "0.0.1"

scalaVersion := "2.12.11"

libraryDependencies ++= Seq(
  "com.bot4s" %% "telegram-core" % "4.4.0-RC2",
  "com.softwaremill.sttp" %% "core" % "1.6.4",
  "com.typesafe.akka" %% "akka-actor" % "2.6.7",
  "de.heikoseeberger" %% "akka-http-json4s" % "1.31.0",
  "com.typesafe.akka" %% "akka-http" % "10.1.12",
  "com.typesafe.akka" %% "akka-stream" % "2.6.7",
  "org.json4s" %% "json4s-native" % "3.6.9",
  "org.json4s" %% "json4s-jackson" % "3.6.9",
  "com.typesafe.akka" %% "akka-slf4j" % "2.6.7",
  "ch.qos.logback" % "logback-classic" % "1.2.3"
)
