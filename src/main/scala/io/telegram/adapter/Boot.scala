package io.telegram.adapter

import akka.actor.{ActorSystem, Props}
import akka.stream.Materializer
import akka.http.scaladsl.Http
import com.typesafe.config.ConfigFactory
import io.telegram.adapter.actors.{ArticlesWorkerActor, ExchangeWorkerActor, GithubWorkerActor, NewsRequesterActor, NewsWorkerActor}
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
  val newsActor = system.actorOf(Props(new NewsWorkerActor()))
  val articlesActor = system.actorOf(Props(new ArticlesWorkerActor()))

  val routes = new Routes()
  val host = config.getString("application.host")
  val port = config.getInt("application.port")

  system.log.info("Starting bot...")

  new TelegramService(token, githubActor, exchangeActor, newsActor, articlesActor).run()

  Http().bindAndHandle(routes.handlers, host, port)

  system.log.info(s"running on $host:$port")

}
