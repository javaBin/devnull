package devnull.rest

import java.util.UUID
import javax.servlet.http.HttpServletRequest

import com.typesafe.scalalogging.LazyLogging
import devnull.ems.{EmsService, EventId, SessionId}
import devnull.rest.helpers.ContentTypeResolver.validContentType
import devnull.rest.helpers.DirectiveHelper.trueOrElse
import devnull.rest.helpers.EitherDirective.{EitherDirective, fromEither, withJson, withTemplate}
import devnull.rest.helpers.JsonCollectionConverter.toFeedback
import devnull.rest.helpers.ResponseWrites.{ResponseCollectionJson, ResponseJson}
import devnull.rest.helpers._
import devnull.storage._
import doobie.imports.toMoreConnectionIOOps
import doobie.util.transactor.Transactor
import net.hamnaberg.json.collection.data.JavaReflectionData
import net.hamnaberg.json.collection.{Item, JsonCollection}
import unfiltered.directives.Directive
import unfiltered.directives.Directives._
import unfiltered.request.{GET, POST}
import unfiltered.response._

import scalaz.concurrent.Task

class SessionFeedbackResource(
    ems: EmsService,
    feedbackRepository: FeedbackRepository,
    paperFeedbackRepository: PaperFeedbackRepository,
    xa: Transactor[Task]) extends LazyLogging {

  type ResponseDirective = Directive[HttpServletRequest, ResponseFunction[Any], ResponseFunction[Any]]

  def handleFeedbacks(eventId: String, sessionId: String): ResponseDirective = {
    val postFeedback = for {
      _ <- POST
      voterInfo <- VoterIdentification.identify()
      contentType <- validContentType
      session <- getOrElse(ems.getSession(EventId(eventId), SessionId(sessionId)), NotFound ~> ResponseString("Didn't find the session in ems"))
      _ <- trueOrElse(ems.canRegisterFeedback(EventId(eventId), SessionId(sessionId)), Forbidden ~> ResponseString("Feedback not open yet!"))
      parsed <- parseFeedback(contentType, session.eventId.id.toString, session.sessionId.id.toString, voterInfo)
      feedback <- fromEither(parsed)
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

    val getFeedback = for {
      _ <- GET
      _ <- getOrElse(ems.getSession(EventId(eventId), SessionId(sessionId)), NotFound ~> ResponseString("Didn't find the session in ems"))
    } yield {
        val sId: UUID = UUID.fromString(sessionId)
        val eId: UUID = UUID.fromString(eventId)
        val response = for {
          sessionOnline <- feedbackRepository.selectFeedbackForSession(sId).transact(xa)
          sessionPaper <- paperFeedbackRepository.selectFeedbackForSession(sId).transact(xa)
          avgPaperEvent <- paperFeedbackRepository.selectAvgFeedbackForEvent(eId).transact(xa)
        } yield GivenFeedbackDto(
          session = FeedbackDto(
            OnlineDto(sessionOnline),
            PaperDto(sessionPaper.map(_.ratings)), sessionPaper.map(_.participants).getOrElse(0)),
          conference = FeedbackDto(
            OnlineDto(0.0, 0.0, 0.0, 0.0, 0),
            PaperDto(avgPaperEvent.map(_._1)), avgPaperEvent.map(_._2).getOrElse(0))
        )
        Ok ~> ResponseJson(response.run)
      }
    postFeedback | getFeedback
  }

  def parseFeedback(contentType: SupportedContentType, eventId:String, sessionId: String, voterInfo: VoterInfo):
  EitherDirective[Either[Throwable, Option[Feedback]]] = {
    contentType match {
      case CollectionJsonContentType => withTemplate(template => toFeedback(template, eventId, sessionId, voterInfo))
      case JsonContentType => withJson { rating: Ratings => Feedback(null, null, voterInfo, UUID.fromString(sessionId), rating) }
    }
  }
}

case class OnlineDto(overall: Double, relevance: Double, content: Double, quality: Double, count: Int)
case class PaperDto(green: Int, yellow: Int, red: Int)
case class FeedbackDto(online: OnlineDto, paper: PaperDto, participants: Int)
case class GivenFeedbackDto(session: FeedbackDto, conference: FeedbackDto)

object OnlineDto {
  def apply(input: Option[FeedbackResult]): OnlineDto = {
    input.map(i => OnlineDto(i.overall, i.relevance, i.content, i.quality, i.count))
        .getOrElse(OnlineDto(0d, 0d, 0d, 0d, 0))
  }
}
object PaperDto {
  def apply(input: Option[PaperRating]): PaperDto = {
    input.map(i => PaperDto(i.green, i.yellow, i.red))
        .getOrElse(PaperDto(0, 0, 0))
  }
}

