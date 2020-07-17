package GithubBot.actors

import GithubBot.actors.RequestActor.{GetUserAccount, GetUserRepositories, GithubRepository, GithubUser}
import GithubBot.actors.TelegramActor.{GetRepositoriesResponse, GetUserResponse}
import GithubBot.packages._
import akka.actor.Actor

import scala.concurrent.Future
import org.json4s.jackson.JsonMethods.parse
import GithubBot.restClient.RestClientImpl._

import scala.annotation.tailrec
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

  def mkListOfString(list: List[GithubRepository]): List[String] = {
    @tailrec
    def innerFunc(lst: List[GithubRepository], res: List[String], acc: Int): List[String] = acc match {
      case -1 => res
      case num: Int => {
        val str = s"${acc + 1}. ${lst(acc).name}: size = ${lst(acc).size}, stargazers = ${lst(acc).stargazers_count}, push date = ${lst(acc).pushed_at}, fork = ${if (lst(acc).fork) "Forked" else "Not Forked"}"
        innerFunc(lst, str :: res, acc - 1)
      }
    }

    innerFunc(list, List(), list.length - 1)
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
          val list = mkListOfString(response)
          val result = if (list.isEmpty) "Sorry, this account does not have any repositories yet!" else list.mkString("\n")
          sender ! GetRepositoriesResponse(
            result
          )
        }
        case Failure(e) => sender ! (
          if (e.getMessage().contains("Expected collection but got JObject")) GetRepositoriesResponse("Account does not exist!")
          else GetRepositoriesResponse("Connection error occured!"))
      }
    }
  }
}
