package exchangebot.actors

import akka.actor.Actor
import exchangebot.actors.RequestActor.{Converted, CurrencyAll, GetAllCurrencies, GetConvertResult}
import exchangebot.actors.TelegramActor.{ConvertFailedResponse, ConvertResponse, GetCurrenciesFailedResponse, GetCurrenciesResponse}
import org.json4s.jackson.JsonMethods.parse
import exchangebot.packages._
import exchangebot.restclient.RestClientImpl._

import scala.collection.immutable.ListMap
import scala.concurrent.Future
import scala.util.{Failure, Success}

object RequestActor {

  case class CurrencyAll(symbols: Map[String, String])

  case class Converted(result: Double)

  case class GetAllCurrencies(msg: String)

  case class GetConvertResult(from: String, to: String, amount: String)

}


class RequestActor extends Actor {

  def getCurrencies(url: String): Future[CurrencyAll] = {
    get(url)
      .map {
        body => parse(body).extract[CurrencyAll]
      }
  }


  def convert(url: String): Future[Converted] = {
    get(url)
      .map {
        body => parse(body).extract[Converted]
      }
  }

  override def receive: Receive = {
    case GetAllCurrencies(msg) => {
      println(msg)
      val sender = context.sender()
      val url = "https://fixer-fixer-currency-v1.p.rapidapi.com/symbols"
      getCurrencies(url).onComplete {
        case Success(response) =>
          val res = ListMap(response.symbols.toSeq.sortBy(_._1): _*)
          sender ! GetCurrenciesResponse(
            res.map(_.productIterator.mkString(": ")).mkString("\n")
          )
        case Failure(e) =>
          sender ! GetCurrenciesFailedResponse(
            s"""Something went wrong! Try again later...
               |Details:
               |${e.getMessage}
               |""".stripMargin)
      }
    }

    case GetConvertResult(from, to, amount) => {
      val sender = context.sender()
      val url = s"https://fixer-fixer-currency-v1.p.rapidapi.com/convert?from=${from}&to=${to}&amount=${amount}"
      convert(url).onComplete {
        case Success(response) => {
          sender ! ConvertResponse(s"${response.result} ${to.toUpperCase()}")
        }
        case Failure(e) =>
          if (e.getMessage.contains("No usable value for result"))
            sender ! ConvertFailedResponse("Some problems occured! May be you wrote an incorrect currency name!")
          else
            sender ! ConvertFailedResponse("Connection problems! Try again later!")
      }
    }
  }
}
