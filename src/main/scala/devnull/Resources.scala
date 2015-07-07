package devnull

import javax.servlet.http.HttpServletRequest

import devnull.storage.{FeedbackId, FeedbackRepository}
import devnull.ResponseWrites.ResponseJson
import doobie.imports._
import doobie.util.transactor.Transactor
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
import scalaz.concurrent.Task

class Resources(val feedbackRepository: FeedbackRepository, xa: Transactor[Task]) extends Plan {
  type ResponseDirective = Directive[HttpServletRequest, ResponseFunction[Any], ResponseFunction[Any]]

  case class Ping(ping: String)

  override def intent: Intent = Intent {
    case Root() => handleRoot()
    case Links.Feedbacks(eventId, sessionId) => handleFeedbacks(eventId, sessionId)
    case _ => failure(NotFound)
  }

  def handleRoot(): ResponseDirective = {
    val get = for {
      _ <- GET
    } yield {
      Ok ~> ResponseJson(Ping("pong"))
    }
    get
  }

  private def withTemplate[T](fromTemplate: (Template) => T): Directive[HttpServletRequest, ResponseFunction[Any], Either[Throwable, T]] = {
    for {
      template <- inputStream.map(is => NativeJsonCollectionParser.parseTemplate(is))
    } yield template.right.map(fromTemplate)
  }

  def contentType(ct: String) = commit(when {
    case RequestContentType(`ct`) => ct
  }.orElse(UnsupportedMediaType))

  def handleFeedbacks(eventId: String, sessionId: String): ResponseDirective = {
    val post = for {
      _ <- POST
      // _ <- contentType("application/vnd.collection+json")
      voterId <- Identification.identify()
      _ <- contentType("application/json")
      parsed <- withTemplate(t => JsonCollectionConverter.toFeedback(t, eventId, sessionId))
      feedback <- fromEither(parsed)
      f <- getOrElse(feedback, BadRequest ~> ResponseString("Feedback did not contain all required fields."))
    } yield {
        println(s"POST => $f from $voterId")
        val feedbackId: FeedbackId = feedbackRepository.insertFeedback(f).transact(xa).run
        Accepted ~> ResponseJson(feedbackId)
    }
    post
  }

  def fromEither[T](either: Either[Throwable, T]): Directive[HttpServletRequest, ResponseFunction[Any], T] = {
    either.fold(
      ex => failure(BadRequest ~> ResponseString(ex.getMessage)),
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

  def apply(feedbackRepository: FeedbackRepository, xa: Transactor[Task]) = new Resources(feedbackRepository, xa)

}
