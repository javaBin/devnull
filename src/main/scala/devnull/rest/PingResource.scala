package devnull.rest

import javax.servlet.http.HttpServletRequest

import devnull.rest.helpers.ResponseWrites.ResponseJson
import unfiltered.directives.Directive
import unfiltered.directives.Directives._
import unfiltered.request.GET
import unfiltered.response.{Ok, ResponseFunction}

object PingResource {

  type ResponseDirective = Directive[HttpServletRequest, ResponseFunction[Any], ResponseFunction[Any]]

  def handlePing(): ResponseDirective = {
    val get = for {
      _ <- GET
    } yield {
        Ok ~> ResponseJson(Ping("pong"))
      }
    get
  }

  case class Ping(ping: String)

}
