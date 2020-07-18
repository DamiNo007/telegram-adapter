package exchangebot

import exchangebot.services.TelegramService
import akka.actor.Props
import exchangebot.actors.TelegramActor
import exchangebot.packages.system

object Boot extends App {
  val telegramActor = system.actorOf(Props(new TelegramActor))
  val token = System.getenv("TELEGRAM_TOKEN")
  println("Starting bot...")
  new TelegramService(token, telegramActor).run()
}
