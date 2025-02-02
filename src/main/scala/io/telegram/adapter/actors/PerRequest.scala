package io.telegram.adapter.actors

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.server.{RequestContext, RouteResult}
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.{DefaultFormats, Formats, Serialization}
import org.json4s.native.Serialization

import scala.concurrent.{ExecutionContext, Promise}

object PerRequest {

  class PerRequestActor(val request: Request,
                        val childProps: Props,
                        val promise: Promise[RouteResult],
                        val requestContext: RequestContext) extends PerRequest

}

trait PerRequest extends Actor with ActorLogging with Json4sSupport {
  implicit val formats: Formats = DefaultFormats
  implicit val serialization: Serialization = Serialization
  implicit val ex: ExecutionContext = context.dispatcher

  val request: Request
  val childProps: Props
  val promise: Promise[RouteResult]
  val requestContext: RequestContext

  context.actorOf(childProps) ! request

  override def receive: Receive = {
    case obj: Response =>
      populateResponse(obj)

  }

  def populateResponse(obj: ToResponseMarshallable): Unit = {
    requestContext
      .complete(obj)
      .onComplete(response => promise.complete(response))

    context.stop(self)
  }

  override def postStop(): Unit =
    log.warning("Stopped...")
}

