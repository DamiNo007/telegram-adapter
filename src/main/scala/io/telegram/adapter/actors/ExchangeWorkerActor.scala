package io.telegram.adapter.actors

import akka.pattern.ask
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.Materializer
import akka.util.Timeout
import io.telegram.adapter.actors.ExchangeRequesterActor._
import org.json4s.{DefaultFormats, Formats}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class ExchangeWorkerActor()(implicit val system: ActorSystem,
                            materializer: Materializer)
    extends Actor {

  implicit val ex: ExecutionContext = context.dispatcher
  implicit val formats: Formats = DefaultFormats
  implicit val timeout: Timeout = 100.seconds

  val requestActor: ActorRef =
    context.actorOf(Props(new ExchangeRequesterActor()))

  override def receive: Receive = {
    case GetCurrencies(msg) =>
      println(msg)
      val sender = context.sender()
      (requestActor ? GetAllCurrencies(msg)).onComplete {
        case Success(value) => sender ! value
        case Failure(e) =>
          sender ! GetCurrenciesFailedResponse(
            s"""Something went wrong! Try again later...
               |Details:
               |${e.getMessage}
               |""".stripMargin
          )
      }
    case Convert(from, to, amount) =>
      val sender = context.sender()
      (requestActor ? GetConvertResult(from, to, amount)).onComplete {
        case Success(value) => sender ! value
        case Failure(e) =>
          sender ! ConvertFailedResponse(
            s"""Something went wrong! Try again later...
               |Details:
               |${e.getMessage}
               |""".stripMargin
          )
      }
  }
}
