package io.telegram.adapter

import io.telegram.adapter.actors.GithubRequesterActor.{GithubRepository, GithubUser}

package object actors {

  trait Response

  trait Request

  case class GetCurrencies(msg: String) extends Request

  case class GetCurrenciesHttp(msg: String) extends Request

  case class Convert(from: String, to: String, amount: String) extends Request

  case class GetUser(login: String) extends Request

  case class GetUserHttp(login: String) extends Request

  case class GetRepositories(login: String) extends Request

  case class GetRepositoriesHttp(login: String) extends Request

  case class GetCurrenciesResponse(response: String) extends Response

  case class GetCurrenciesResponseHttp(symbols: Map[String, String]) extends Response

  case class GetCurrenciesFailedResponse(error: String) extends Response

  case class ConvertResponse(response: String) extends Response

  case class ConvertFailedResponse(error: String) extends Response

  case class GetUserResponse(response: String) extends Response

  case class GetUserHttpResponse(user: GithubUser) extends Response

  case class GetRepositoriesHttpResponse(list: List[GithubRepository]) extends Response

  case class GetUserFailedResponse(error: String) extends Response

  case class GetRepositoriesResponse(response: String) extends Response

  case class GetRepositoriesFailedResponse(error: String) extends Response

}
