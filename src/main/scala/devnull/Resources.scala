package devnull

import javax.servlet.http.HttpServletRequest

import devnull.rest._
import devnull.sessions.SessionService
import devnull.storage.{FeedbackRepository, PaperFeedbackRepository}
import doobie.util.transactor.Transactor
import linx.Root
import unfiltered.directives.Directives._
import unfiltered.directives._
import unfiltered.filter.Plan
import unfiltered.filter.Plan.Intent
import unfiltered.filter.request.ContextPath
import unfiltered.request._
import unfiltered.response._
import io.mth.unfiltered.cors.{Cors, CorsConfig}

import scala.language.implicitConversions
import scalaz.concurrent.Task

class Resources(feedbackResource: SessionFeedbackResource, eventFeedbackResource: EventFeedbackResource) extends Plan {

  val cors = Cors(
    CorsConfig(
      (origin: String) => {true},
      (method: String) => List("POST", "GET").contains(method),
      (headers: List[String]) => true,
      allowCredentials = true,
      maxAge = Some(120),
      "Voter-ID" :: "Content-Type" :: Nil
    )
  )

  override def intent: Intent = cors(Intent {
    case Links.AppRoot() => PingResource.handlePing()
    case Links.AppInfo() => AppInfo.handelAppInfo()
    case Links.Event(eventId) => eventFeedbackResource.handleFeedback(eventId)
    case Links.Feedbacks(eventId, sessionId) => feedbackResource.handleFeedbacks(eventId, sessionId)
    case _ => failure(NotFound)
  })

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

  def apply(
      emsService: SessionService,
      feedbackRepository: FeedbackRepository,
      paperFeedbackRepository: PaperFeedbackRepository,
      xa: Transactor[Task]) = {
    val feedbackResource: SessionFeedbackResource = new SessionFeedbackResource(
      emsService, feedbackRepository, paperFeedbackRepository, xa)
    val eventFeedbackResource: EventFeedbackResource = new EventFeedbackResource(paperFeedbackRepository, xa)
    new Resources(feedbackResource, eventFeedbackResource)
  }

}
