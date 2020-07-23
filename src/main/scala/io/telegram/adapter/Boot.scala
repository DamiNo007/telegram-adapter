package io.telegram.adapter

import akka.actor.{ActorSystem, Props}
import akka.stream.Materializer
import akka.http.scaladsl.Http
import com.typesafe.config.ConfigFactory
import io.telegram.adapter.actors.{ExchangeWorkerActor, GithubWorkerActor}
import io.telegram.adapter.http.routes.Routes
import io.telegram.adapter.services.TelegramService

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

object Boot extends App {
  implicit val system: ActorSystem = ActorSystem("tbActorSystem")
  implicit val materializer: Materializer =
    Materializer.createMaterializer(system)
  implicit val executionContext: ExecutionContextExecutor =
    ExecutionContext.global
  val config = ConfigFactory.load()
  val token = config.getString("telegram-token")
  val githubActor = system.actorOf(Props(new GithubWorkerActor()))
  val exchangeActor = system.actorOf(Props(new ExchangeWorkerActor()))

  val routes = new Routes()
  val host = config.getString("application.host")
  val port = config.getInt("application.port")

  println("Starting bot...")

  new TelegramService(token, githubActor, exchangeActor).run()

  Http().bindAndHandle(routes.handlers, host, port)

  println(s"running on $host:$port")

}
