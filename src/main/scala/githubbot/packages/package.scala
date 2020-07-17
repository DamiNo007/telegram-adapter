package githubbot

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.json4s.DefaultFormats

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

package object packages {
  implicit val system: ActorSystem = ActorSystem("tbActorSystem")
  implicit val materializer: ActorMaterializer = ActorMaterializer.create(system)
  implicit val executionContext: ExecutionContextExecutor = ExecutionContext.global
  implicit val defaultFormats: DefaultFormats.type = DefaultFormats
}
