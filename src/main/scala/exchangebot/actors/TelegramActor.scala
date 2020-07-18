package exchangebot.actors

import scala.util.Failure
import scala.util.Success
import akka.actor.{Actor, ActorRef, Props}
import akka.util.Timeout
import akka.pattern.ask
import exchangebot.actors.RequestActor.{GetAllCurrencies, GetConvertResult}
import exchangebot.actors.TelegramActor.{Convert, ConvertFailedResponse, GetCurrencies, GetCurrenciesFailedResponse}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.concurrent.duration._

object TelegramActor {

  trait Response

  case class GetCurrencies(msg: String)

  case class Convert(from: String, to: String, amount: String)

  case class GetCurrenciesResponse(response: String) extends Response

  case class ConvertResponse(response: String) extends Response

  case class GetCurrenciesFailedResponse(response: String) extends Response

  case class ConvertFailedResponse(response: String) extends Response

}

class TelegramActor extends Actor {

  implicit val executionContext: ExecutionContextExecutor = ExecutionContext.global
  implicit val timeout: Timeout = 100.seconds
  val requestActor = context.actorOf(Props(new RequestActor))

  override def receive: Receive = {
    case GetCurrencies(msg) => {
      println(msg)
      val sender = context.sender()
      (requestActor ? GetAllCurrencies(msg)).onComplete {
        case Success(value) => sender ! value
        case Failure(e) => {
          sender ! GetCurrenciesFailedResponse(
            s"""Something went wrong! Try again later...
               |Details:
               |${e.getMessage}
               |""".stripMargin)
        }
      }
    }
    case Convert(from, to, amount) => {
      val sender = context.sender()
      (requestActor ? GetConvertResult(
        from,
        to,
        amount
      )).onComplete {
        case Success(value) => sender ! value
        case Failure(e) => {
          sender ! ConvertFailedResponse(
            s"""Something went wrong! Try again later...
               |Details:
               |${e.getMessage}
               |""".stripMargin)
        }
      }
    }
  }
}