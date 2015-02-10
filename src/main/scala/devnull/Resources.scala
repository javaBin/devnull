package devnull

import javax.servlet.http.HttpServletRequest

import linx.Root
import net.hamnaberg.json.collection.{NativeJsonCollectionParser, Template}
import unfiltered.directives.Directives._
import unfiltered.directives._
import unfiltered.filter.Plan
import unfiltered.filter.Plan.Intent
import unfiltered.filter.request.ContextPath
import unfiltered.request._
import unfiltered.response._

import scala.language.implicitConversions

class Resources extends Plan {
  type ResponseDirective = Directive[HttpServletRequest, ResponseFunction[Any], ResponseFunction[Any]]

  override def intent: Intent = Intent {
    case Root() => handleRoot()
    case Links.Feedbacks(eventId, sessionId) => handleFeedbacks(eventId, sessionId)
    case _ => failure(NotFound)
  }

  def handleRoot(): ResponseDirective = {
    val get = for {
      _ <- GET
    } yield {
      JsonContent ~> ResponseString( """{ "ping":"ok"} """)
    }
    get
  }

  private def withTemplate[T](fromTemplate: (Template) => T): Directive[HttpServletRequest, ResponseFunction[Any], Either[Throwable, T]] = {
    for {
      template <- inputStream.map(is => NativeJsonCollectionParser.parseTemplate(is))
    } yield template.right.map(fromTemplate)
  }

  def toFeedback(template: Template, eventId: String, sessionId: String): Feedback = {
    val overall = template.getPropertyValue("overall").map(_.value.toString.toInt).get
    val relevance = template.getPropertyValue("relevance").map(_.value.toString.toInt).get
    val content = template.getPropertyValue("content").map(_.value.toString.toInt).get
    val quality = template.getPropertyValue("quality").map(_.value.toString.toInt).get
    Feedback(eventId, sessionId, overall, relevance, content, quality)
  }

  def contentType(ct: String) = commit(when {
    case RequestContentType(`ct`) => ct
  }.orElse(UnsupportedMediaType))


  case class Feedback(eventId: String, sessionId: String, overall: Int, relevance: Int, content: Int, quality: Int)

  def handleFeedbacks(eventId: String, sessionId: String): ResponseDirective = {
    val post = for {
      _ <- POST
      // _ <- contentType("application/vnd.collection+json")
      _ <- contentType("application/json")
      parsed <- withTemplate(t => toFeedback(t, eventId, sessionId))
      feedback <- parsed
    } yield {
      println(s"POST => $feedback ")
      Accepted // todo return a feedback id.
    }
    post
  }

  implicit def fromEither[T](either: Either[Throwable, T]): Directive[HttpServletRequest, ResponseFunction[Any], T] = {
    either.fold(
      ex => failure(InternalServerError ~> ResponseString(ex.getMessage)),
      a => success(a)
    )
  }

  val Intent = Mapping[String] { case ContextPath(_, path) => path}

  case class Mapping[X](from: HttpRequest[HttpServletRequest] => X) {
    def apply(intent: PartialFunction[X, Directive[HttpServletRequest, ResponseFunction[Any], ResponseFunction[Any]]]): unfiltered.Cycle.Intent[HttpServletRequest, Any] = Directive.Intent {
        case req if intent.isDefinedAt(from(req)) => intent(from(req))
      }
  }

}

object Resources {

  def apply() = new Resources()

}
