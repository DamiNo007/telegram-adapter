package io.telegram.adapter.http.routes

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.server.{RequestContext, Route, RouteResult}
import akka.util.Timeout

import scala.concurrent.{ExecutionContext, Future, Promise}
import akka.http.scaladsl.server.Directives._
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import io.telegram.adapter.actors.{Convert, ExchangeWorkerActor, GetCurrenciesHttp, GetRatesHttp, GetRepositoriesHttp, GetUserHttp, GithubWorkerActor, Request}
import io.telegram.adapter.actors.PerRequest.PerRequestActor
import org.json4s.jackson.Serialization
import org.json4s.{DefaultFormats, Formats, Serialization}

import scala.concurrent.duration._


class Routes()(implicit ex: ExecutionContext, system: ActorSystem) extends Json4sSupport {
  implicit val formats: Formats = DefaultFormats
  implicit val serialization: Serialization = Serialization
  implicit val timeout: Timeout = 5.seconds

  val handlers: Route = pathPrefix("api") {
    pathPrefix("github") {
      pathPrefix(Segment) { username =>
        path("repos") { ctx =>
          val body = GetRepositoriesHttp(username)
          completeRequest(body, ctx, GithubWorkerActor.props)
        } ~ get { ctx =>
          val body = GetUserHttp(username)
          completeRequest(body, ctx, GithubWorkerActor.props)
        }
      }
    } ~ pathPrefix("exchange") {
      pathPrefix("currencies") {
        get { ctx =>
          val body = GetCurrenciesHttp("currencies")
          completeRequest(body, ctx, ExchangeWorkerActor.props)
        }
      } ~ pathPrefix("convert") {
        post {
          entity(as[Convert]) { body =>
            ctx =>
              completeRequest(body, ctx, ExchangeWorkerActor.props)
          }
        }
      } ~ pathPrefix("rates") {
        pathPrefix(Segment) { currency =>
          get { ctx =>
            val body = GetRatesHttp(currency)
            completeRequest(body, ctx, ExchangeWorkerActor.props)
          }
        }
      }
    }
  }

  def completeRequest(body: Request,
                      ctx: RequestContext,
                      props: Props): Future[RouteResult] = {
    val promise = Promise[RouteResult]
    system.actorOf(Props(new PerRequestActor(body, props, promise, ctx)))
    promise.future
  }
}
