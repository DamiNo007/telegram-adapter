package io.telegram.adapter.actors

import akka.actor.{Actor, ActorSystem}
import akka.stream.Materializer
import io.telegram.adapter.Boot.config
import io.telegram.adapter.actors.GithubRequesterActor._
import org.json4s.{DefaultFormats, Formats}
import org.json4s.jackson.JsonMethods.parse
import io.telegram.adapter.utils.RestClientImpl._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object GithubRequesterActor {

  case class GetUserAccount(login: String)

  case class GetUserRepositories(login: String)

  case class GithubUser(login: String,
                        name: String,
                        avatarUrl: Option[String],
                        publicRepos: Option[String])

  case class GithubRepository(name: String,
                              size: Int,
                              fork: Boolean,
                              pushedAt: String,
                              stargazersCount: Int)

}

class GithubRequesterActor()(implicit val system: ActorSystem,
                             materializer: Materializer)
  extends Actor {

  implicit val ex: ExecutionContext = context.dispatcher
  implicit val formats: Formats = DefaultFormats

  val baseUrl = config.getString("github.base-url")

  //  https://api.github.com/users/{$USER}
  def getGithubUser(username: String): Future[GithubUser] = {
    get(s"${baseUrl}/users/${username}", Nil)
      .map { body =>
        parse(body).camelizeKeys.extract[GithubUser]
      }
  }

  def getUserRepositories(username: String): Future[List[GithubRepository]] = {
    get(s"${baseUrl}/users/${username}/repos", Nil)
      .map { body =>
        parse(body).camelizeKeys.extract[List[GithubRepository]]
      }
  }

  def mkListOfString(list: List[GithubRepository]): List[String] = {
    list.zipWithIndex.map {
      case (repoInfo, id) =>
        s"${id + 1}. ${repoInfo.name}: size = ${repoInfo.size}, stargazers = ${repoInfo.stargazersCount}, push date = ${repoInfo.pushedAt}, fork = ${
          if (repoInfo.fork)
            "Forked"
          else "Not Forked"
        }"
    }
  }

  override def receive: Receive = {
    case GetUserAccount(login) =>
      val sender = context.sender()
      getGithubUser(login).onComplete {
        case Success(user) =>
          sender ! GetUserResponse(
            s"""
               |Full name: ${user.name}
               |Login: ${user.login}
               |Avatar:  ${user.avatarUrl.getOrElse("None")}
               |Repositories:  ${
              user.publicRepos
                .getOrElse("None")
            } """.stripMargin)
        case Failure(e) =>
          sender ! (if (e.getMessage().contains("No usable value for login"))
            GetUserFailedResponse("Account does not exist!")
          else GetUserFailedResponse("Connection error occured!"))
      }
    case GetUserRepositories(login) =>
      val sender = context.sender()
      getUserRepositories(login).onComplete {
        case Success(response) =>
          val list = mkListOfString(response)
          val result =
            if (list.isEmpty)
              "Sorry, this account does not have any repositories yet!"
            else list.mkString("\n")
          sender ! GetRepositoriesResponse(result)
        case Failure(e) =>
          sender ! (if (e.getMessage()
            .contains("Expected collection but got JObject"))
            GetRepositoriesFailedResponse("Account does not exist!")
          else
            GetRepositoriesFailedResponse(
              "Connection error occured!"
            ))
      }
  }
}
