package io.telegram.adapter

package object actors {
  trait Response
  trait Request

  case class GetCurrencies(msg: String) extends Request
  case class Convert(from: String, to: String, amount: String) extends Request
  case class GetUser(login: String) extends Request
  case class GetRepositories(login: String) extends Request

  case class GetCurrenciesResponse(response: String) extends Response
  case class ConvertResponse(response: String) extends Response
  case class GetCurrenciesFailedResponse(response: String) extends Response
  case class ConvertFailedResponse(response: String) extends Response
  case class GetUserResponse(response: String) extends Response
  case class GetUserFailedResponse(response: String) extends Response
  case class GetRepositoriesResponse(response: String) extends Response
  case class GetRepositoriesFailedResponse(response: String) extends Response
}
