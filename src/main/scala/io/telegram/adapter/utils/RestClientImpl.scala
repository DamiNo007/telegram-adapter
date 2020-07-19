package io.telegram.adapter.utils

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model._
import akka.stream.Materializer
import akka.util.ByteString

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

object RestClientImpl {

  def parseResponse(
    response: HttpResponse
  )(implicit materializer: Materializer, ex: ExecutionContext): Future[String] =
    response.entity.toStrict(10.seconds).flatMap { entity =>
      entity.dataBytes
        .runFold(ByteString.empty)((acc, bytes) => acc ++ bytes)
        .map(_.utf8String)
    }

  def get(url: String,
          headers: List[RawHeader],
          query: Option[Map[String, String]] = None)(
    implicit system: ActorSystem,
    materializer: Materializer,
    ex: ExecutionContext
  ): Future[String] = {
    val request = HttpRequest(
      method = HttpMethods.GET,
      uri = url,
      headers = List(
        RawHeader("x-rapidapi-host", s"${System.getenv("API_HOST")}"),
        RawHeader("x-rapidapi-key", s"${System.getenv("API_KEY")}")
      )
    )
    Http().singleRequest(request).flatMap(parseResponse)
  }

  def post[T](url: String,
              data: T,
              query: Option[Map[String, String]] = None,
              headers: Option[Map[String, String]] = None)(
    implicit system: ActorSystem,
    materializer: Materializer,
    ex: ExecutionContext
  ): Future[String] = {
    val request = HttpRequest(
      method = HttpMethods.POST,
      uri = url,
      entity =
        HttpEntity(contentType = ContentTypes.`application/json`, data.toString)
    )
    Http().singleRequest(request).flatMap(parseResponse)
  }

}
