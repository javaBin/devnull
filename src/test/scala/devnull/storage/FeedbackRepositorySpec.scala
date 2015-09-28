package devnull.storage

import java.util.UUID

import devnull.TestTags.DatabaseTag
import doobie.imports._
import doobie.util.transactor.DriverManagerTransactor
import org.scalatest.{BeforeAndAfter, FunSpec, Matchers}

import scalaz.concurrent.Task

class FeedbackRepositorySpec extends FunSpec with BeforeAndAfter with Matchers with DatabaseMigration {

  val cfg = DatabaseConfigEnv()
  val xa = DriverManagerTransactor[Task](cfg.driver, cfg.connectionUrl, cfg.username, cfg.password)
  val repo = new FeedbackRepository()

  after {
    sql"delete from feedback".update.run.transact(xa).run
  }

  describe("Postgres database") {

    it("should insert a feedback", DatabaseTag) {
      val response: FeedbackId = repo.insertFeedback(FeedbackTestData.createFeedback()).transact(xa).run

      response.feedbackId should be > 0
    }

    it("should query an inserted feedback", DatabaseTag) {
      repo.insertFeedback(FeedbackTestData.createFeedback()).transact(xa).run

      val feedbacks: List[Feedback] = repo.selectFeedbacks().transact(xa).run

      feedbacks should have size 1
    }

    it("should get average of session feedback") {
      val sessionId: UUID = UUID.randomUUID()
      val info_1 = VoterInfo("nr 1", "127.0.0.1", "spec")
      val info_2 = VoterInfo("nr 2", "127.0.0.1", "spec")
      val insertTestData = for {
        r1_ignored <- repo.insertFeedback(Feedback(null, null, info_1, sessionId, Ratings(2, Some(3), Some(4), Some(5)))).transact(xa)
        r1 <- repo.insertFeedback(Feedback(null, null, info_1, sessionId, Ratings(2, Some(3), Some(4), Some(5)))).transact(xa)
        r2 <- repo.insertFeedback(Feedback(null, null, info_2, sessionId, Ratings(5, Some(4), Some(3), Some(2)))).transact(xa)
      } yield s"Inserted $r1 and $r2"
      insertTestData.run

      val result: Option[FeedbackResult] = repo.selectFeedbackForSession(sessionId).transact(xa).run

      result should not be empty
      result.get.count should be(2)

      result.get.overall should be(3.5 +- 0.01)
      result.get.content should be(3.5 +- 0.01)
      result.get.quality should be(3.5 +- 0.01)
      result.get.relevance should be(3.5 +- 0.01)
    }
  }

}
