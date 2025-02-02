package io.telegram.adapter

import io.telegram.adapter.actors.ArticlesRequesterActor.ArticleItem
import io.telegram.adapter.actors.ExchangeRequesterActor.Rates
import io.telegram.adapter.actors.GithubRequesterActor.{GithubRepository, GithubUser}
import io.telegram.adapter.actors.NewsRequesterActor.{News, NewsItem}

package object actors {

  trait Response

  trait Request

  case class GetCurrencies(msg: String) extends Request

  case class GetCurrenciesHttp(msg: String) extends Request

  case class GetRates(currency: String) extends Request

  case class GetRatesHttp(currency: String) extends Request

  case class Convert(from: String, to: String, amount: String) extends Request

  case class GetUser(login: String) extends Request

  case class GetUserHttp(login: String) extends Request

  case class GetRepositories(login: String) extends Request

  case class GetRepositoriesHttp(login: String) extends Request

  case class GetCurrenciesResponse(response: String) extends Response

  case class GetCurrenciesResponseHttp(symbols: Map[String, String]) extends Response

  case class GetRatesResponse(response: String) extends Response

  case class GetRatesHttpResponse(rates: List[Rates]) extends Response

  case class GetRatesFailedResponse(error: String) extends Response

  case class GetCurrenciesFailedResponse(error: String) extends Response

  case class ConvertResponse(response: String) extends Response

  case class ConvertFailedResponse(error: String) extends Response

  case class GetUserResponse(response: String) extends Response

  case class GetUserHttpResponse(user: GithubUser) extends Response

  case class GetRepositoriesHttpResponse(list: List[GithubRepository]) extends Response

  case class GetUserFailedResponse(error: String) extends Response

  case class GetRepositoriesResponse(response: String) extends Response

  case class GetRepositoriesFailedResponse(error: String) extends Response

  case class GetNews(msg: String) extends Request

  case class GetNewsHttp(msg: String) extends Request

  case class GetNewsResponse(response: String) extends Response

  case class GetNewsHttpResponse(news: List[NewsItem]) extends Response

  case class GetNewsFailedResponse(error: String) extends Response

  case class GetArticles(msg: String) extends Request

  case class GetArticlesHttp(msg: String) extends Request

  case class GetArticlesResponse(response: String) extends Response

  case class GetArticlesHttpResponse(articles: List[ArticleItem]) extends Response

  case class GetArticlesFailedResponse(error: String) extends Response

}
