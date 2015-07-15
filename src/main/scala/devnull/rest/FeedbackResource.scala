package devnull.rest

import javax.servlet.http.HttpServletRequest

import devnull.rest.helpers.ContentTypeResolver.withContentType
import devnull.rest.helpers.EitherDirective.{fromEither, withTemplate}
import devnull.rest.helpers.JsonCollectionConverter.toFeedback
import devnull.rest.helpers.ResponseWrites.ResponseJson
import devnull.rest.helpers._
import devnull.storage.{FeedbackId, FeedbackRepository}
import doobie.imports.toMoreConnectionIOOps
import doobie.util.transactor.Transactor
import unfiltered.directives.Directive
import unfiltered.directives.Directives._
import unfiltered.request.POST
import unfiltered.response.{Accepted, BadRequest, ResponseFunction, ResponseString}

import scalaz.concurrent.Task

class FeedbackResource(feedbackRepository: FeedbackRepository, xa: Transactor[Task]) {

  type ResponseDirective = Directive[HttpServletRequest, ResponseFunction[Any], ResponseFunction[Any]]

  def handleFeedbacks(eventId: String, sessionId: String): ResponseDirective = {
    val post = for {
      _ <- POST
      // _ <- contentType("application/vnd.collection+json")
      voterInfo <- VoterIdentification.identify()
      _ <- withContentType("application/json")
      parsed <- withTemplate(template => toFeedback(template, eventId, sessionId, voterInfo))
      feedback <- fromEither(parsed)
      f <- getOrElse(feedback, BadRequest ~> ResponseString("Feedback did not contain all required fields."))
    } yield {
        println(s"POST => $f from $voterInfo")
        val feedbackId: FeedbackId = feedbackRepository.insertFeedback(f).transact(xa).run
        Accepted ~> ResponseJson(feedbackId)
      }
    post
  }

}
