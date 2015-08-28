package devnull

import javax.servlet.http.HttpServletRequest

import devnull.ems.EmsService
import devnull.rest.{AppInfo, FeedbackResource, PingResource}
import devnull.storage.FeedbackRepository
import doobie.util.transactor.Transactor
import linx.Root
import unfiltered.directives.Directives._
import unfiltered.directives._
import unfiltered.filter.Plan
import unfiltered.filter.Plan.Intent
import unfiltered.filter.request.ContextPath
import unfiltered.request._
import unfiltered.response._

import scala.language.implicitConversions
import scalaz.concurrent.Task

class Resources(feedbackResource: FeedbackResource) extends Plan {

  override def intent: Intent = Intent {
    case Root() => PingResource.handlePing()
    case Links.AppInfo() => AppInfo.handelAppInfo()
    case Links.Feedbacks(eventId, sessionId) => feedbackResource.handleFeedbacks(eventId, sessionId)
    case _ => failure(NotFound)
  }

  val Intent = Mapping[String] {
    case ContextPath(_, path) => path
  }

  case class Mapping[X](from: HttpRequest[HttpServletRequest] => X) {
    type ResponseDirective = Directive[HttpServletRequest, ResponseFunction[Any], ResponseFunction[Any]]

    def apply(intent: PartialFunction[X, ResponseDirective]): unfiltered.Cycle.Intent[HttpServletRequest, Any] =
      Directive.Intent {
        case req if intent.isDefinedAt(from(req)) => intent(from(req))
      }
  }

}

object Resources {

  def apply(emsService: EmsService, feedbackRepository: FeedbackRepository, xa: Transactor[Task]) = {
    val feedbackResource: FeedbackResource = new FeedbackResource(emsService, feedbackRepository, xa)
    new Resources(feedbackResource)
  }

}
