package devnull.storage

import devnull.TestTags.DatabaseTag
import doobie.imports._
import doobie.util.transactor.DriverManagerTransactor
import org.scalatest.{Matchers, BeforeAndAfter, FunSpec}

import scalaz.concurrent.Task

class PaperFeedbackSpec extends FunSpec with BeforeAndAfter with Matchers with DatabaseMigration  {

  val cfg = DatabaseConfigEnv()
  val xa = DriverManagerTransactor[Task](cfg.driver, cfg.connectionUrl, cfg.username, cfg.password)
  val repo = new PaperFeedbackRepository()

  describe("Postgres database") {

    it("should insert a paper feedback", DatabaseTag) {
      val response: FeedbackId = repo.insertPaperFeedback(FeedbackTestData.createPaperFeedback()).transact(xa).run

      response.feedbackId should be > 0
    }

    it("should fetch a saved session paper feedback", DatabaseTag) {
      val fb = FeedbackTestData.createPaperFeedback()

      repo.insertPaperFeedback(fb).transact(xa).run

      val sessionFeedback: Option[PaperFeedback] = repo.selectFeedbackForSession(fb.sessionId).transact(xa).run

      sessionFeedback should not be empty
    }
  }
}
