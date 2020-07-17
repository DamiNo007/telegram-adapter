package GithubBot.actors

import GithubBot.actors.RequestActor.{GetUserAccount, GetUserRepositories}
import GithubBot.actors.TelegramActor.{GetRepositories, GetRepositoriesResponse, GetUser, GetUserResponse}

import scala.util.Failure
import scala.util.Success
import akka.actor.{Actor, ActorRef, Props}
import akka.util.Timeout
import akka.pattern.ask

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.concurrent.duration._

object TelegramActor {

  case class GetUser(login: String)

  case class GetUserResponse(response: String)

  case class GetRepositories(login: String)

  case class GetRepositoriesResponse(response: String)

}

class TelegramActor extends Actor {

  implicit val executionContext: ExecutionContextExecutor = ExecutionContext.global
  implicit val timeout: Timeout = 100.seconds
  val requestActor = context.actorOf(Props(new RequestActor))

  override def receive: Receive = {
    case GetUser(login) => {
      val sender = context.sender()
      (requestActor ? GetUserAccount(login)).onComplete {
        case Success(value) => sender ! value
        case Failure(e) => {
          sender ! GetUserResponse(e.getMessage)
        }
      }
      requestActor ! GetUserAccount(login)
    }
    case GetRepositories(login) => {
      val sender = context.sender()
      (requestActor ? GetUserRepositories(login)).onComplete {
        case Success(value) => sender ! value
        case Failure(e) => {
          sender ! GetRepositoriesResponse(e.getMessage)
        }
      }
    }
  }

}
