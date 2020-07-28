package io.telegram.adapter.actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.stream.Materializer
import akka.pattern.ask
import akka.util.Timeout
import io.telegram.adapter.actors.NewsRequesterActor.{GetNewsAll, GetNewsAllHttp}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object NewsWorkerActor {
  def props()(implicit system: ActorSystem, materializer: Materializer): Props =
    Props(new NewsWorkerActor())
}

class NewsWorkerActor()(implicit val system: ActorSystem,
                        materializer: Materializer)
  extends Actor with ActorLogging {

  implicit val timeout: Timeout = 100.seconds
  implicit val ex: ExecutionContext = context.dispatcher
  val requestActor = context.actorOf(Props(new NewsRequesterActor))

  override def receive: Receive = {
    case GetNews(msg) =>
      val sender = context.sender()
      (requestActor ? GetNewsAll(msg)).onComplete {
        case Success(value) =>
          sender ! value
        case Failure(e) =>
          sender ! GetNewsFailedResponse(e.getMessage)
      }
    case GetNewsHttp(msg) =>
      val sender = context.sender()
      (requestActor ? GetNewsAllHttp(msg)).onComplete {
        case Success(value) =>
          log.info(s"received response $value")
          sender ! value
        case Failure(e) =>
          log.warning(s"received error response ${e.getMessage}")
          sender ! GetNewsFailedResponse(e.getMessage)
      }
  }
}