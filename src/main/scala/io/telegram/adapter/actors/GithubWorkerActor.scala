package io.telegram.adapter.actors

import akka.actor.{Actor, ActorSystem, Props}
import akka.pattern.ask
import akka.stream.Materializer
import akka.util.Timeout
import io.telegram.adapter.actors.GithubRequesterActor._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object GithubWorkerActor {
  def props()(implicit system: ActorSystem, materializer: Materializer): Props =
    Props(new GithubWorkerActor())
}

class GithubWorkerActor()(implicit val system: ActorSystem,
                          materializer: Materializer)
  extends Actor {

  implicit val timeout: Timeout = 100.seconds
  implicit val ex: ExecutionContext = context.dispatcher
  val requestActor = context.actorOf(Props(new GithubRequesterActor))

  override def receive: Receive = {
    case GetUser(login) =>
      val sender = context.sender()
      (requestActor ? GetUserAccount(login)).onComplete {
        case Success(value) =>
          sender ! value
        case Failure(e) =>
          sender ! GetUserFailedResponse(e.getMessage)
      }
    case GetRepositories(login) =>
      val sender = context.sender()
      (requestActor ? GetUserRepositories(login)).onComplete {
        case Success(value) => sender ! value
        case Failure(e) =>
          sender ! GetUserFailedResponse(e.getMessage)
      }

    case GetUserHttp(login) =>
      val sender = context.sender()
      (requestActor ? GetUserAccountHttp(login)).onComplete {
        case Success(value) => sender ! value
        case Failure(e) =>
          sender ! GetUserFailedResponse(e.getMessage)
      }

    case GetRepositoriesHttp(login) =>
      val sender = context.sender()
      (requestActor ? GetUserRepositoriesHttp(login)).onComplete {
        case Success(value) =>
          println(value)
          sender ! value
        case Failure(e) =>
          sender ! GetUserFailedResponse(e.getMessage)
      }
  }
}
