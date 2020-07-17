package GithubBot.actors

import GithubBot.actors.RequestActor.{GetUserAccount, GetUserRepositories, GithubRepository, GithubUser}
import GithubBot.actors.TelegramActor.{GetRepositoriesResponse, GetUserResponse}
import GithubBot.packages._
import akka.actor.Actor

import scala.concurrent.Future
import org.json4s.jackson.JsonMethods.parse
import GithubBot.restClient.RestClientImpl._

import scala.util.Failure
import scala.util.Success

object RequestActor {


  case class GetUserAccount(login: String)

  case class GetUserRepositories(login: String)

  case class GithubUser(login: String, name: String, avatarUrl: Option[String], public_repos: Option[String])

  case class GithubRepository(name: String, size: Int, fork: Boolean, pushed_at: String, stargazers_count: Int)

}

class RequestActor extends Actor {

  //  https://api.github.com/users/{$USER}
  def getGithubUser(username: String): Future[GithubUser] = {
    get(s"https://api.github.com/users/$username")
      .map {
        body => parse(body).extract[GithubUser]
      }
  }

  def getUserRepositories(username: String): Future[List[GithubRepository]] = {
    get(s"https://api.github.com/users/${username}/repos")
      .map {
        body => parse(body).extract[List[GithubRepository]]
      }
  }


  override def receive: Receive = {
    case GetUserAccount(login) => {
      val sender = context.sender()
      getGithubUser(login).onComplete {
        case Success(user) => {
          sender ! GetUserResponse(
            s"""
               |Full name: ${user.name}
               |Login: ${user.login}
               |Avatar:  ${user.avatarUrl.getOrElse("None")}
               |Repositories:  ${user.public_repos.getOrElse("None")} """.stripMargin
          )
        }
        case Failure(e) => sender ! (
          if (e.getMessage().contains("No usable value for login")) GetUserResponse("Account does not exist!")
          else GetUserResponse("Connection error occured!"))
      }
    }
    case GetUserRepositories(login) => {
      val sender = context.sender()
      getUserRepositories(login).onComplete {
        case Success(response) => {
          sender ! GetRepositoriesResponse(
            response.mkString("\n")
          )
        }
        case Failure(e) => sender ! (
          if (e.getMessage().contains("Expected collection but got JObject")) GetRepositoriesResponse("Account does not exist!")
          else GetRepositoriesResponse("Connection error occured!"))
      }
    }
  }
}
