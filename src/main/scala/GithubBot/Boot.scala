package GithubBot

import GithubBot.services.TelegramService
import akka.actor.Props
import packages._
import actors._

object Boot extends App {
  val telegramActor = system.actorOf(Props(new TelegramActor))
  val token = System.getenv("TELEGRAM_TOKEN")
  println("Starting bot...")
  new TelegramService(token, telegramActor).run()
}
