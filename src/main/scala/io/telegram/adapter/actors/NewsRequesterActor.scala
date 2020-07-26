package io.telegram.adapter.actors

import akka.actor.{Actor, ActorLogging, ActorSystem}
import akka.stream.Materializer
import com.lucidchart.open.xtract.XmlReader.seq
import io.telegram.adapter.Boot.config
import org.json4s.{DefaultFormats, Formats}
import io.telegram.adapter.utils.RestClientImpl._

import scala.concurrent.ExecutionContext
import com.lucidchart.open.xtract.{XmlReader, __}
import com.lucidchart.open.xtract.XmlReader._
import cats.syntax.all._
import io.telegram.adapter.actors.NewsRequesterActor.{GetNewsAll, GetNewsAllHttp, News, NewsItem}

object NewsRequesterActor {

  case class News(
                   items: Seq[NewsItem]
                 )

  case class NewsItem(
                       title: String,
                       description: String,
                       link: String
                     )

  object NewsItem {
    implicit val reader: XmlReader[NewsItem] = (
      (__ \ "title").read[String],
      (__ \ "description").read[String],
      (__ \ "link").read[String]
      ).mapN(apply _)
  }

  object News {
    implicit val reader: XmlReader[News] = (
      (__ \ "channel" \ "item").read(seq[NewsItem])
      ).map(apply _)
  }

  case class GetNewsAll(msg: String)

  case class GetNewsAllHttp(msg: String)

}

class NewsRequesterActor()(implicit val system: ActorSystem,
                           val materializer: Materializer)
  extends Actor with ActorLogging {

  implicit val ex: ExecutionContext = context.dispatcher
  implicit val formats: Formats = DefaultFormats

  val baseUrl = config.getString("profitKZ.base-url")

  def mkListString(list: List[NewsItem]): List[String] = {
    list.zipWithIndex.map {
      case (NewsItem(title, description, link), id) =>
        s"""
           |${id + 1}. Title: $title
           |Description: $description
           |Link: $link""".stripMargin
    }
  }

  def getNews: List[NewsItem] = {
    val xml = getXml(s"$baseUrl/news")
    val parseRes = XmlReader.of[News].read(xml).getOrElse("unknown")
    val items = parseRes.asInstanceOf[News].items.toList
    items
  }

  override def receive: Receive = {
    case GetNewsAll(msg) =>
      log.info(s"got message $msg")
      val sender = context.sender()
      val items = getNews
      val result = mkListString(items.take(5))
      result match {
        case head :: tail =>
          sender ! GetNewsResponse(result.mkString("\n"))
        case _ =>
          sender ! GetNewsResponse("No news found")
      }
    case GetNewsAllHttp(msg) =>
      log.info(s"got message $msg")
      val sender = context.sender()
      val items = getNews
      items match {
        case head :: tail =>
          sender ! GetNewsHttpResponse(items)
        case _ =>
          sender ! GetNewsResponse("No news found")
      }
  }
}
