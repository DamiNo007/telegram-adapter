package io.telegram.adapter.actors

import akka.pattern.ask
import akka.actor.{Actor, ActorRef, ActorLogging, ActorSystem, Props}
import akka.stream.Materializer
import akka.util.Timeout
import io.telegram.adapter.actors.ExchangeRequesterActor._
import org.json4s.{DefaultFormats, Formats}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object ExchangeWorkerActor {
  def props()(implicit system: ActorSystem, materializer: Materializer): Props =
    Props(new ExchangeWorkerActor())
}

class ExchangeWorkerActor()(implicit val system: ActorSystem,
                            materializer: Materializer)
  extends Actor with ActorLogging {

  implicit val ex: ExecutionContext = context.dispatcher
  implicit val formats: Formats = DefaultFormats
  implicit val timeout: Timeout = 100.seconds

  val requestActor: ActorRef =
    context.actorOf(Props(new ExchangeRequesterActor()))

  override def receive: Receive = {
    case GetCurrencies(msg) =>
      log.info(s"got msg $msg")
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

    case GetRates(currency) =>
      val sender = context.sender()
      (requestActor ? GetRatesAll(currency)).onComplete {
        case Success(value) => sender ! value
        case Failure(e) =>
          sender ! GetRatesFailedResponse(
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

    case GetCurrenciesHttp(msg) =>
      log.info(s"got msg $msg")
      val sender = context.sender()
      (requestActor ? GetAllCurrenciesHttp(msg)).onComplete {
        case Success(value) => sender ! value
        case Failure(e) =>
          sender ! GetCurrenciesFailedResponse(
            s"""Something went wrong! Try again later...
               |Details:
               |${e.getMessage}
               |""".stripMargin
          )
      }

    case GetRatesHttp(currency) =>
      val sender = context.sender()
      (requestActor ? GetRatesAllHttp(currency)).onComplete {
        case Success(value) => sender ! value
        case Failure(e) =>
          sender ! GetRatesFailedResponse(
            s"""Something went wrong! Try again later...
               |Details:
               |${e.getMessage}
               |""".stripMargin
          )
      }
  }
}
