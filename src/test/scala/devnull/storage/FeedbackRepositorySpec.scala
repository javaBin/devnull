package devnull.storage

import java.util.UUID

import devnull.TestTags.DatabaseTag
import doobie.imports._
import doobie.util.transactor.DriverManagerTransactor
import org.scalatest.{BeforeAndAfter, FunSpec, Matchers}

import scalaz.concurrent.Task

class FeedbackRepositorySpec extends FunSpec with BeforeAndAfter with Matchers with DatabaseMigration {

  val cfg = DatabaseConfigEnv()
  val xa = DriverManagerTransactor[Task](cfg.driver, cfg.connectionUrl, cfg.username, cfg.password.value)
  val repo = new FeedbackRepository()

  after {
    sql"delete from feedback".update.run.transact(xa).unsafePerformSync
  }

  describe("Postgres database") {

    it("should insert a feedback", DatabaseTag) {
      val response: FeedbackId = repo.insertFeedback(FeedbackTestData.createFeedback()).transact(xa).unsafePerformSync

      response.feedbackId should be > 0
    }

    it("should query an inserted feedback", DatabaseTag) {
      repo.insertFeedback(FeedbackTestData.createFeedback()).transact(xa).unsafePerformSync

      val feedbacks: List[Feedback] = repo.selectFeedbacks().transact(xa).unsafePerformSync

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
      insertTestData.unsafePerformSync

      val result: Option[FeedbackResult] = repo.selectFeedbackForSession(sessionId).transact(xa).unsafePerformSync

      result should not be empty
      result.get.count should be(Some(2))

      result.get.overall should be(Some(3.5))
      result.get.content should be(Some(3.5))
      result.get.quality should be(Some(3.5))
      result.get.relevance should be(Some(3.5))
    }

    it("should get average of event feedback") {
      val paperRepo: PaperFeedbackRepository = new PaperFeedbackRepository()
      val eventIdOne: UUID = UUID.randomUUID()
      val sessionIdOne: UUID = UUID.randomUUID()
      val sessionIdTwo: UUID = UUID.randomUUID()

      val eventIdTwo: UUID = UUID.randomUUID()
      val sessionIdThree: UUID = UUID.randomUUID()

      val insert = for {
        // Test data we should match on
        t1 <- paperRepo.insertPaperFeedback(PaperFeedback(null, null, eventIdOne, sessionIdOne, PaperRating(20, 10, 5), 200)).transact(xa)
        t2 <- paperRepo.insertPaperFeedback(PaperFeedback(null, null, eventIdOne, sessionIdTwo, PaperRating(20, 10, 5), 200)).transact(xa)
        t3 <- repo.insertFeedback(FeedbackTestData.createFeedback(sessionIdOne)).transact(xa)
        t4 <- repo.insertFeedback(FeedbackTestData.createFeedback(sessionIdOne)).transact(xa)
        t5 <- repo.insertFeedback(FeedbackTestData.createFeedback(sessionIdTwo)).transact(xa)

        // Test data that we should not match on
        t6 <- paperRepo.insertPaperFeedback(PaperFeedback(null, null, eventIdTwo, sessionIdThree, PaperRating(20, 10, 5), 100)).transact(xa)
        t7 <- repo.insertFeedback(FeedbackTestData.createFeedback(sessionIdThree)).transact(xa)

        t8 <- repo.insertFeedback(FeedbackTestData.createFeedback(voterId = "12345")).transact(xa)
      } yield "done"
      insert.unsafePerformSync

      val result = repo.selectFeedbackForEvent(eventIdOne).transact(xa).unsafePerformSync

      result should not be empty
      result.get.count should be(Some(2))
    }

    describe("comments") {

      it("should return empty list when no result ", DatabaseTag) {
        val sessionId: UUID = UUID.randomUUID()
        repo.insertFeedback(FeedbackTestData.createFeedback(session = sessionId, comments = None)).transact(xa).unsafePerformSync

        val comments: List[String] = repo.selectComments(sessionId).transact(xa).unsafePerformSync

        comments should be(empty)
      }

      it("should only show comment for the given session", DatabaseTag) {
        val sessionId: UUID = UUID.randomUUID()
        repo.insertFeedback(FeedbackTestData.createFeedback(session = sessionId, comments = Some("Session one"))).transact(xa).unsafePerformSync
        repo.insertFeedback(FeedbackTestData.createFeedback(session = UUID.randomUUID(), comments = Some("Session two"))).transact(xa).unsafePerformSync

        val comments: List[String] = repo.selectComments(sessionId).transact(xa).unsafePerformSync

        comments should be("Session one" :: Nil)
      }

      it("should return the last comment for a [session, voterId]", DatabaseTag) {
        val sessionId: UUID = UUID.randomUUID()
        repo.insertFeedback(FeedbackTestData.createFeedback(session = sessionId, comments = Some("Fist comment"))).transact(xa).unsafePerformSync
        repo.insertFeedback(FeedbackTestData.createFeedback(session = sessionId, comments = Some("Last comment"))).transact(xa).unsafePerformSync

        val comments: List[String] = repo.selectComments(sessionId).transact(xa).unsafePerformSync

        comments should be("Last comment" :: Nil)
      }

      it("should return list of comments for unique votes", DatabaseTag) {
        val sessionId: UUID = UUID.randomUUID()
        repo.insertFeedback(FeedbackTestData.createFeedback(session = sessionId, voterId = "1234", comments = Some("One"))).transact(xa).unsafePerformSync
        repo.insertFeedback(FeedbackTestData.createFeedback(session = sessionId, voterId = "4321", comments = Some("Two"))).transact(xa).unsafePerformSync

        val comments: List[String] = repo.selectComments(sessionId).transact(xa).unsafePerformSync

        comments.sorted should be("One" :: "Two" :: Nil)
      }
    }
  }
}
