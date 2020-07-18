package exchangebot.services

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import cats.instances.future._
import cats.syntax.functor._
import com.bot4s.telegram.api.RequestHandler
import com.bot4s.telegram.api.declarative.Commands
import com.bot4s.telegram.clients.FutureSttpClient
import com.bot4s.telegram.future.{Polling, TelegramBot}
import com.softwaremill.sttp.SttpBackend
import com.softwaremill.sttp.okhttp.OkHttpFutureBackend
import exchangebot.actors.TelegramActor.{GetCurrencies, GetCurrenciesFailedResponse, GetCurrenciesResponse}
import exchangebot.actors.TelegramActor._

import scala.concurrent.Future
import scala.concurrent.duration._

class TelegramService(token: String, workerActor: ActorRef)
  extends TelegramBot
    with Polling
    with Commands[Future] {

  implicit val timeout: Timeout = 20.seconds
  implicit val backend: SttpBackend[Future, Nothing] = OkHttpFutureBackend()
  override val client: RequestHandler[Future] = new FutureSttpClient(token)

  onCommand("/start") { implicit msg =>
    println(s"получил комманду ${msg.text}")
    reply("Привет, чем могу вам помочь?").void
  }

  onCommand("/help") { implicit msg =>
    println(s"получил комманду ${msg.text}")
    reply(
      """
        |/start - starts the bot
        |/help - describes each command
        |/currencies - gets the list of currencies
        |/convert <from> <to> <amount> - converts one currency to another
        |""".stripMargin).void
  }

  onCommand("/currencies") { implicit msg =>
    println(s"получил комманду ${msg.text}")
    (workerActor ? GetCurrencies(msg.text.toString))
      .mapTo[Response]
      .map {
        case res: GetCurrenciesResponse => reply(res.response)
        case res: GetCurrenciesFailedResponse => reply(res.response)
      }
      .void
  }

  onCommand("/convert") { implicit msg =>
    println(s"получил комманду ${msg.text}")
    val msgSplit = msg.text.getOrElse("unknown").split(" ")
    msgSplit.length match {
      case 4 =>
        (workerActor ? Convert(msgSplit(1).toUpperCase(), msgSplit(2).toUpperCase(), msgSplit(3).toUpperCase()))
          .mapTo[Response]
          .map {
            case res: ConvertResponse => reply(res.response)
            case res: ConvertFailedResponse => reply(res.response)
          }
          .void
      case _ => reply("Incorrect command! Example: RUB KZT 100").void
    }
  }

  onMessage { implicit msg =>
    if (!msg.text.getOrElse("").startsWith("/")) {
      println(s"получил ${msg.text}")
      reply(s"ECHO: ${msg.text.getOrElse("")}").void
    } else Future()
  }
}
