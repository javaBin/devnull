package devnull.rest

import java.util.Date
import javax.servlet.http.HttpServletRequest

import devnull.rest.helpers.ResponseWrites.ResponseJson
import unfiltered.directives.Directive
import unfiltered.directives.Directives._
import unfiltered.request.GET
import unfiltered.response.{Ok, ResponseFunction}

object AppInfo {
  type ResponseDirective = Directive[HttpServletRequest, ResponseFunction[Any], ResponseFunction[Any]]

  def handelAppInfo(): ResponseDirective = {
    val get = for {
      _ <- GET
    } yield {
        Ok ~> ResponseJson(AppInfoContent(
          devnull.BuildInfo.scalaVersion,
          devnull.BuildInfo.version,
          devnull.BuildInfo.buildTime,
          devnull.BuildInfo.branch,
          devnull.BuildInfo.sha
        ))
      }

    get
  }

  case class AppInfoContent(
      scalaVersion: String,
      version: String,
      buildTime: String,
      branch: String,
      sha: String)

}
