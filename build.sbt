name := "TelegramAdapter"

version := "0.0.1"

scalaVersion := "2.12.11"

val akkaVersion = "2.6.7"
val jsonVersion = "3.6.9"

libraryDependencies ++= Seq(
  "com.bot4s" %% "telegram-core" % "4.4.0-RC2",
  "com.softwaremill.sttp" %% "core" % "1.6.4",
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "de.heikoseeberger" %% "akka-http-json4s" % "1.31.0",
  "com.typesafe.akka" %% "akka-http" % "10.1.12",
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "org.json4s" %% "json4s-native" % jsonVersion,
  "org.json4s" %% "json4s-jackson" % jsonVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3"
)
