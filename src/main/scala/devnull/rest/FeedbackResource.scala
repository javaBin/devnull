package devnull.rest

import java.util.UUID
import javax.servlet.http.HttpServletRequest

import com.typesafe.scalalogging.LazyLogging
import devnull.ems.{SessionId, EventId, EmsService}
import devnull.rest.helpers.ContentTypeResolver.validContentType
import devnull.rest.helpers.DirectiveHelper.trueOrElse
import devnull.rest.helpers.EitherDirective.{EitherDirective, fromEither, withJson, withTemplate}
import devnull.rest.helpers.JsonCollectionConverter.toFeedback
import devnull.rest.helpers.ResponseWrites.{ResponseCollectionJson, ResponseJson}
import devnull.rest.helpers._
import devnull.storage._
import doobie.imports.toMoreConnectionIOOps
import doobie.util.transactor.Transactor
import net.hamnaberg.json.collection.{JsonCollection, Item}
import net.hamnaberg.json.collection.data.JavaReflectionData
import unfiltered.directives.Directive
import unfiltered.directives.Directives._
import unfiltered.request.POST
import unfiltered.response._

import scalaz.concurrent.Task

class FeedbackResource(
    ems: EmsService,
    feedbackRepository: FeedbackRepository,
    xa: Transactor[Task]) extends LazyLogging {

  type ResponseDirective = Directive[HttpServletRequest, ResponseFunction[Any], ResponseFunction[Any]]

  def handleFeedbacks(eventId: String, sessionId: String): ResponseDirective = {
    val postJson = for {
      _ <- POST
      voterInfo <- VoterIdentification.identify()
      contentType <- validContentType
      parsed <- parseFeedback(contentType, eventId, sessionId, voterInfo)
      feedback <- fromEither(parsed)
      _ <- getOrElse(ems.getSession(EventId(eventId), SessionId(sessionId)), NotFound ~> ResponseString("Didn't find the session in ems"))
      _ <- trueOrElse(ems.canRegisterFeedback(EventId(eventId), SessionId(sessionId)), Forbidden ~> ResponseString("Feedback not open yet!"))
      f <- getOrElse(feedback, BadRequest ~> ResponseString("Feedback did not contain all required fields."))
    } yield {
        logger.debug(s"POST => $f from $voterInfo")
        val feedbackId: FeedbackId = feedbackRepository.insertFeedback(f).transact(xa).run
        Accepted ~> {
          contentType match {
            case JsonContentType => ResponseJson(feedbackId)
            case CollectionJsonContentType => {
              implicit val formats = org.json4s.DefaultFormats
              implicit val extractor = new JavaReflectionData[FeedbackId]
              val item = Item(java.net.URI.create(""), feedbackId, Nil)
              ResponseCollectionJson(JsonCollection(item))
            }
          }
        }
      }
    postJson
  }

  def parseFeedback(contentType: SupportedContentType, eventId:String, sessionId: String, voterInfo: VoterInfo):
  EitherDirective[Either[Throwable, Option[Feedback]]] = {
    contentType match {
      case CollectionJsonContentType => withTemplate(template => toFeedback(template, eventId, sessionId, voterInfo))
      case JsonContentType => withJson { rating: Ratings => Feedback(null, null, voterInfo, UUID.fromString(sessionId), rating) }
    }
  }
}
