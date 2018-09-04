package devnull.storage

import java.util.UUID

import devnull.TestTags.DatabaseTag
import doobie.imports._
import org.scalatest.{BeforeAndAfter, FunSpec, Matchers}

import scalaz.concurrent.Task

class PaperFeedbackRepositorySpec
    extends FunSpec
    with BeforeAndAfter
    with Matchers
    with DatabaseMigration {

  val cfg = DatabaseConfigEnv()
  val xa = DriverManagerTransactor[Task](
    cfg.driver,
    cfg.connectionUrl,
    cfg.username,
    cfg.password.value
  )
  val repo = new PaperFeedbackRepository()

  describe("Postgres database") {

    it("should insert a paper feedback", DatabaseTag) {
      val response: FeedbackId = repo
        .insertPaperFeedback(FeedbackTestData.createPaperFeedback())
        .transact(xa)
        .unsafePerformSync

      response.feedbackId should be > 0
    }

    it("should fetch a saved session paper feedback", DatabaseTag) {
      val fb = FeedbackTestData.createPaperFeedback()

      repo.insertPaperFeedback(fb).transact(xa).unsafePerformSync

      val sessionFeedback: Option[PaperFeedback] =
        repo.selectFeedbackForSession(fb.sessionId).transact(xa).unsafePerformSync

      sessionFeedback should not be empty
    }

    it("should query for avg feedback for an event", DatabaseTag) {
      val eventId: UUID = UUID.randomUUID
      val resultOfOne: PaperFeedback =
        FeedbackTestData.createPaperFeedback(eventId = eventId)

      repo.insertPaperFeedback(resultOfOne).transact(xa).unsafePerformSync
      repo
        .insertPaperFeedback(FeedbackTestData.createPaperFeedback(eventId = eventId))
        .transact(xa)
        .unsafePerformSync

      val result: Option[(PaperRatingResult, Option[Double])] =
        repo.selectAvgFeedbackForEvent(eventId).transact(xa).unsafePerformSync

      result should not be empty
      val (ratings, participants) = result.get
      participants should be(Some(resultOfOne.participants))
      ratings.green should be(Some(resultOfOne.ratings.green))
      ratings.yellow should be(Some(resultOfOne.ratings.yellow))
      ratings.red should be(Some(resultOfOne.ratings.red))
    }
  }

}
