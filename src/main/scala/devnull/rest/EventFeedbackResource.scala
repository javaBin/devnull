package devnull.rest

import java.util.UUID
import javax.servlet.http.HttpServletRequest

import devnull.rest.helpers.ContentTypeResolver.withContentType
import devnull.rest.helpers.EitherDirective._
import devnull.rest.helpers.ResponseWrites.ResponseJson
import devnull.storage.{PaperRating, PaperFeedback, PaperFeedbackRepository}
import doobie.imports.toMoreConnectionIOOps
import doobie.util.transactor.Transactor
import org.json4s._
import org.json4s.native.JsonMethods
import unfiltered.directives.Directive
import unfiltered.directives.Directives._
import unfiltered.request.POST
import unfiltered.response.{Accepted, ResponseFunction}

import scalaz.concurrent.Task

class EventFeedbackResource(paperFeedbackRepository: PaperFeedbackRepository, xa: Transactor[Task]) {
  type ResponseDirective = Directive[HttpServletRequest, ResponseFunction[Any], ResponseFunction[Any]]

  def handleFeedback(eventId: String): ResponseDirective = {
    for {
      _ <- POST
      _ <- withContentType("application/json")
      paperFeedbacks <- toJson[FeedbackWrapper]
    } yield {
      val pfe = paperFeedbacks.feedbacks.map{ e => ToPaperFeedback(eventId, e)}
      pfe.foreach{pfb => paperFeedbackRepository.insertPaperFeedback(pfb).transact(xa)}
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
