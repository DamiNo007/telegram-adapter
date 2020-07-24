package io.telegram.adapter.services

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
import io.telegram.adapter.actors._

import scala.concurrent.Future
import scala.concurrent.duration._

class TelegramService(token: String,
                      githubWorkerActor: ActorRef,
                      exchangeWorkerActor: ActorRef)
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
        |/getGithubUser <login> - gets user's data
        |/getUserRepositories <login> - gets user's repositories with description
        |/currencies - gets the list of currencies
        |/rates <currency> - gets current currency rates
        |/convert <from> <to> <amount> - converts one currency to another
        |""".stripMargin
    ).void
  }

  onCommand("/getGithubUser") { implicit msg =>
    (githubWorkerActor ? GetUser(
      msg.text.map(x => x.split(" ").last.trim).getOrElse("unknown")
    )).mapTo[Response]
      .map {
        case res: GetUserResponse => reply(res.response)
        case res: GetUserFailedResponse => reply(res.error)
      }
      .void
  }

  onCommand("/getUserRepositories") { implicit msg =>
    (githubWorkerActor ? GetRepositories(
      msg.text.map(x => x.split(" ").last.trim).getOrElse("unknown")
    )).mapTo[Response]
      .map {
        case res: GetRepositoriesResponse => reply(res.response)
        case res: GetRepositoriesFailedResponse => reply(res.error)
      }
      .void
  }

  onCommand("/currencies") { implicit msg =>
    println(s"получил комманду ${msg.text}")
    (exchangeWorkerActor ? GetCurrencies(msg.text.toString))
      .mapTo[Response]
      .map {
        case res: GetCurrenciesResponse => reply(res.response)
        case res: GetCurrenciesFailedResponse => reply(res.error)
      }
      .void
  }

  onCommand("/rates") { implicit msg =>
    (exchangeWorkerActor ? GetRates(
      msg.text.map(x => x.split(" ").last.trim).getOrElse("unknown")
    )).mapTo[Response]
      .map {
        case res: GetRatesResponse => reply(res.response)
        case res: GetRatesFailedResponse => reply(res.error)
      }
      .void
  }

  onCommand("/convert") { implicit msg =>
    println(s"получил комманду ${msg.text}")
    val msgSplit = msg.text.getOrElse("unknown").split(" ")
    msgSplit.length match {
      case 4 =>
        (exchangeWorkerActor ? Convert(
          msgSplit(1).toUpperCase(),
          msgSplit(2).toUpperCase(),
          msgSplit(3).toUpperCase()
        )).mapTo[Response]
          .map {
            case res: ConvertResponse => reply(res.response)
            case res: ConvertFailedResponse => reply(res.error)
          }
          .void
      case _ => reply("Incorrect command! Example: /convert RUB KZT 100").void
    }
  }

  onMessage { implicit msg =>
    if (!msg.text.getOrElse("").startsWith("/")) {
      println(s"получил ${msg.text}")
      reply(s"ECHO: ${msg.text.getOrElse("")}").void
    } else Future()
  }
}
