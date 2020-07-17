package githubbot.actors

import githubbot.actors.RequestActor.{GetUserAccount, GetUserRepositories}
import githubbot.actors.TelegramActor.{GetRepositories, GetRepositoriesResponse, GetUser, GetUserFailedResponse, GetUserResponse}

import scala.util.Failure
import scala.util.Success
import akka.actor.{Actor, ActorRef, Props}
import akka.util.Timeout
import akka.pattern.ask

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.concurrent.duration._

object TelegramActor {

  trait Response

  case class GetUser(login: String)

  case class GetRepositories(login: String)

  case class GetUserResponse(response: String) extends Response

  case class GetUserFailedResponse(response: String) extends Response

  case class GetRepositoriesResponse(response: String) extends Response

  case class GetRepositoriesFailedResponse(response: String) extends Response

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
          sender ! GetUserFailedResponse(e.getMessage)
        }
      }
    }
    case GetRepositories(login) => {
      val sender = context.sender()
      (requestActor ? GetUserRepositories(login)).onComplete {
        case Success(value) => sender ! value
        case Failure(e) => {
          sender ! GetUserFailedResponse(e.getMessage)
        }
      }
    }
  }
}
