package io.telegram.adapter.actors

import akka.actor.{Actor, ActorSystem}
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.Materializer
import io.telegram.adapter.Boot.config
import io.telegram.adapter.actors.ExchangeRequesterActor._
import io.telegram.adapter.utils.RestClientImpl.get
import org.json4s.{DefaultFormats, Formats}
import org.json4s.jackson.JsonMethods.parse

import scala.collection.immutable.ListMap
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object ExchangeRequesterActor {

  case class CurrencyAll(symbols: Map[String, String])

  case class Converted(result: Double)

  case class GetAllCurrencies(msg: String)

  case class GetConvertResult(from: String, to: String, amount: String)

}

class ExchangeRequesterActor()(implicit val system: ActorSystem,
                               materializer: Materializer)
  extends Actor {

  implicit val ex: ExecutionContext = context.dispatcher
  implicit val formats: Formats = DefaultFormats

  val baseUrl = config.getString("exchange.base-url")
  val apiHost = config.getString("exchange.api-host")
  val apiKey = config.getString("exchange.api-key")

  def getCurrencies(url: String): Future[CurrencyAll] = {
    get(url, List(RawHeader("x-rapidapi-host", apiHost), RawHeader("x-rapidapi-key", apiKey)))
      .map { body =>
        parse(body).extract[CurrencyAll]
      }
  }

  def convert(url: String): Future[Converted] = {
    get(url, List(RawHeader("x-rapidapi-host", apiHost), RawHeader("x-rapidapi-key", apiKey)))
      .map { body =>
        parse(body).extract[Converted]
      }
  }

  override def receive: Receive = {
    case GetAllCurrencies(msg) =>
      println(msg)
      val sender = context.sender()
      val url = s"${baseUrl}/symbols"
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
               |""".stripMargin
          )
      }

    case GetConvertResult(from, to, amount) =>
      val sender = context.sender()
      val url =
        s"${baseUrl}/convert?from=${from}&to=${to}&amount=${amount}"
      convert(url).onComplete {
        case Success(response) =>
          sender ! ConvertResponse(s"${response.result} ${to.toUpperCase()}")
        case Failure(e) =>
          if (e.getMessage.contains("No usable value for result"))
            sender ! ConvertFailedResponse(
              "Some problems occured! May be you wrote an incorrect currency name!"
            )
          else
            sender ! ConvertFailedResponse(
              "Connection problems! Try again later!"
            )
      }
  }
}
