package devnull.rest

import java.util.UUID
import javax.servlet.http.HttpServletRequest

import com.typesafe.scalalogging.LazyLogging
import devnull.UuidFromString
import devnull.rest.helpers.ContentTypeResolver._
import devnull.rest.helpers.DirectiveHelper.trueOrElse
import devnull.rest.helpers.EitherDirective.{EitherDirective, fromEither, withJson, withTemplate}
import devnull.rest.helpers.JsonCollectionConverter.toFeedback
import devnull.rest.helpers.ResponseWrites.{ResponseCollectionJson, ResponseJson}
import devnull.rest.helpers._
import devnull.sessions.{EventId, SessionId, SessionService}
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
    sessionService: SessionService,
    feedbackRepository: FeedbackRepository,
    paperFeedbackRepository: PaperFeedbackRepository,
    xa: Transactor[Task]
) extends LazyLogging {

  type ResponseDirective = Directive[HttpServletRequest, ResponseFunction[Any], ResponseFunction[Any]]

  def handleFeedbacks(eventIdStr: String, sessionIdStr: String): ResponseDirective = {
    val postFeedback = for {
      _ <- POST
      voterInfo <- VoterIdentification.identify()
      contentType <- withContentTypes(List(MIMEType.Json, MIMEType.CollectionJson))
      eventId <- fromEither(UuidFromString(eventIdStr).right.map(EventId.apply))
      sessionId <- fromEither(UuidFromString(sessionIdStr).right.map(SessionId.apply))
      session <- getOrElse(sessionService.getSession(eventId, sessionId), NotFound ~> ResponseString("Didn't find the session"))
      _ <- trueOrElse(sessionService.canRegisterFeedback(eventId, sessionId), Forbidden ~> ResponseString("Feedback not open yet!"))
      parsed <- parseFeedback(contentType, session.eventId, session.sessionId, voterInfo)
      feedback <- fromEither(parsed)
      f <- getOrElse(feedback, BadRequest ~> ResponseString("Feedback did not contain all required fields."))
    } yield {
      logger.debug(s"POST => $f from $voterInfo")
      val feedbackId: FeedbackId = feedbackRepository.insertFeedback(f).transact(xa).run
      Accepted ~> {
        contentType match {
          case MIMEType("application", "json", _) => ResponseJson(feedbackId)
          case MIMEType("application", "vnd.collection+json", _) => {
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
      eventId <- fromEither(UuidFromString(eventIdStr).right.map(EventId.apply))
      sessionId <- fromEither(UuidFromString(sessionIdStr).right.map(SessionId.apply))
      _ <- getOrElse(sessionService.getSession(eventId, sessionId), NotFound ~> ResponseString("Didn't find the session"))
    } yield {
      val sId: UUID = sessionId.id
      val eId: UUID = eventId.id
      val response = for {
        sessionOnlineFeedback <- feedbackRepository.selectFeedbackForSession(sId).transact(xa)
        sessionPaper <- paperFeedbackRepository.selectFeedbackForSession(sId).transact(xa)
        avgConferenceOnlineFeedback <- feedbackRepository.selectFeedbackForEvent(eId).transact(xa)
        avgPaperEvent <- paperFeedbackRepository.selectAvgFeedbackForEvent(eId).transact(xa)
        comments <- feedbackRepository.selectComments(sId).transact(xa)
      } yield {
        val (paperDto: PaperDto, participants: Int) = avgPaperEvent.map { case (f: PaperRatingResult, i: Option[Double]) =>
          (PaperDto(f.green.getOrElse(0), f.yellow.getOrElse(0), f.red.getOrElse(0)), i.getOrElse(0d).toInt)
        }.getOrElse((PaperDto(0, 0, 0), 0))
        GivenFeedbackDto(
          session = FeedbackDto(
            OnlineDto(sessionOnlineFeedback),
            sessionPaper.map(f => PaperDto(f.ratings.green, f.ratings.yellow, f.ratings.red)).getOrElse(PaperDto(0, 0, 0)),
            sessionPaper.map(_.participants).getOrElse(0)),
          conference = FeedbackDto(
            OnlineDto(avgConferenceOnlineFeedback),
            paperDto,
            participants
          ),
          comments
        )
      }
      Ok ~> ResponseJson(response.run)
    }
    postFeedback | getFeedback
  }

  def parseFeedback(contentType: MIMEType, eventId: EventId, sessionId: SessionId, voterInfo: VoterInfo):
  EitherDirective[Either[Throwable, Option[Feedback]]] = {
    contentType match {
      case MIMEType("application", "json", _) => withJson { rating: Ratings => Feedback(null, null, voterInfo, sessionId.id, rating) }
      case MIMEType("application", "vnd.collection+json", _) => withTemplate(template => toFeedback(template, eventId, sessionId, voterInfo))
    }
  }
}

case class OnlineDto(overall: Double, relevance: Double, content: Double, quality: Double, count: Double)
case class PaperDto(green: Double, yellow: Double, red: Double)
case class FeedbackDto(online: OnlineDto, paper: PaperDto, participants: Int)
case class GivenFeedbackDto(session: FeedbackDto, conference: FeedbackDto, comments: List[String])

object OnlineDto {
  def apply(input: Option[FeedbackResult]): OnlineDto = {
    input.map(i => OnlineDto(i.overall.getOrElse(0), i.relevance.getOrElse(0), i.content.getOrElse(0), i.quality.getOrElse(0), i.count.getOrElse(0)))
        .getOrElse(OnlineDto(0d, 0d, 0d, 0d, 0))
  }
}


