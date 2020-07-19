package io.telegram.adapter

import akka.actor.{ActorSystem, Props}
import akka.stream.Materializer
import com.typesafe.config.ConfigFactory
import io.telegram.adapter.actors.{ExchangeWorkerActor, GithubWorkerActor}
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

  println("Starting bot...")

  new TelegramService(token, githubActor, exchangeActor).run()

}
