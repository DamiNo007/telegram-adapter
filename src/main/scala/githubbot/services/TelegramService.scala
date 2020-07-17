package githubbot.services

import scala.util.Failure
import scala.util.Success
import akka.actor.{Actor, ActorRef}
import akka.pattern.ask
import akka.util.Timeout
import com.bot4s.telegram.api.RequestHandler
import com.bot4s.telegram.api.declarative.Commands
import com.bot4s.telegram.future.{Polling, TelegramBot}
import com.bot4s.telegram.clients.FutureSttpClient
import com.softwaremill.sttp.SttpBackend
import com.softwaremill.sttp.okhttp.OkHttpFutureBackend
import cats.instances.future._
import cats.syntax.functor._
import githubbot.actors.RequestActor.GithubRepository
import githubbot.actors.TelegramActor.{GetRepositories, GetRepositoriesFailedResponse, GetRepositoriesResponse, GetUser, GetUserFailedResponse, GetUserResponse, Response}

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
        |/getGithubUser <login> - gets user's data
        |/getUserRepositories <login> - gets user's repositories with description
        |""".stripMargin).void
  }

  onCommand("/getGithubUser") { implicit msg =>
    (workerActor ? GetUser(
      msg.text.map(x => x.split(" ").last.trim).getOrElse("unknown")
    )).mapTo[Response]
      .map(response => {
        if (response.isInstanceOf[GetUserResponse])
          reply(response.asInstanceOf[GetUserResponse].response)
        else
          reply(response.asInstanceOf[GetUserFailedResponse].response)
      })
      .void
  }

  onCommand("/getUserRepositories") { implicit msg =>
    (workerActor ? GetRepositories(
      msg.text.map(x => x.split(" ").last.trim).getOrElse("unknown")
    )).mapTo[Response]
      .map(response => {
        if (response.isInstanceOf[GetRepositoriesResponse])
          reply(response.asInstanceOf[GetRepositoriesResponse].response)
        else
          reply(response.asInstanceOf[GetRepositoriesFailedResponse].response)
      })
      .void
  }

  onMessage { implicit msg =>
    if (!msg.text.getOrElse("").startsWith("/")) {
      println(s"получил ${msg.text}")
      reply(s"ECHO: ${msg.text.getOrElse("")}").void
    } else Future()
  }

}
