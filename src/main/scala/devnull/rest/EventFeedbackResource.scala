package devnull.rest

import java.util.UUID
import javax.servlet.http.HttpServletRequest

import devnull.rest.helpers.ContentTypeResolver.withContentTypes
import devnull.rest.helpers.EitherDirective._
import devnull.rest.helpers.ResponseWrites.ResponseJson
import devnull.storage.{PaperFeedback, PaperFeedbackRepository, PaperRating}
import doobie.imports.toMoreConnectionIOOps
import doobie.util.transactor.Transactor
import org.json4s._
import org.json4s.native.JsonMethods
import unfiltered.directives.Directive
import unfiltered.directives.Directives._
import unfiltered.request.{POST, StringHeader}
import unfiltered.response.{Accepted, ResponseFunction, Unauthorized}

import scala.util.Properties._
import scalaz.concurrent.Task

class EventFeedbackResource(paperFeedbackRepository: PaperFeedbackRepository, xa: Transactor[Task]) {
  type ResponseDirective = Directive[HttpServletRequest, ResponseFunction[Any], ResponseFunction[Any]]

  def handleFeedback(eventId: String): ResponseDirective = {
    for {
      _ <- POST
      _ <- withContentTypes(List(MIMEType.Json))
      _ <- hasAdminTokenToken
      paperFeedbacks <- toJson[FeedbackWrapper]
    } yield {
      val pfe = paperFeedbacks.feedbacks.map { e => ToPaperFeedback(eventId, e) }
      pfe.foreach { pfb => paperFeedbackRepository.insertPaperFeedback(pfb).transact(xa).run }
      Accepted ~> ResponseJson(FeedbackResponse(pfe.size))
    }
  }

  def toJson[T : Manifest]:EitherDirective[T] = {
    inputStream.map(is => {
      implicit val formats = org.json4s.DefaultFormats
      val parse: JValue = JsonMethods.parse(new StreamInput(is))
      parse.extract[T]
    })
  }

  def hasAdminTokenToken = commit {
    val secret: Option[String] = propOrNone("admin-secret").orElse(envOrNone("ADMIN_SECRET"))
    when { case Token(token) if secret.contains(token) => () }.orElse(Unauthorized)
  }

  case object Token extends StringHeader("Token")

}

case class PaperFeedbackEntry(sessionId: String, green: Int, yellow: Int, red: Int, participants: Int)
case class FeedbackWrapper(feedbacks: List[PaperFeedbackEntry])
case class FeedbackResponse(numInserted: Int)

object ToPaperFeedback {
  def apply(eventId: String, entry: PaperFeedbackEntry): PaperFeedback = {
    PaperFeedback(
      null,
      null,
      UUID.fromString(eventId),
      UUID.fromString(entry.sessionId),
      PaperRating(entry.green, entry.yellow, entry.red),
      entry.participants)
  }
}
