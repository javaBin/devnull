package devnull.storage

import devnull.TestTags.DatabaseTag
import doobie.imports._
import doobie.util.transactor.DriverManagerTransactor
import org.scalatest.{BeforeAndAfter, FunSpec, Matchers}

import scalaz.concurrent.Task

class FeedbackRepositorySpec extends FunSpec with BeforeAndAfter with Matchers {

  val cfg = DatabaseConfig()
  val xa = DriverManagerTransactor[Task](cfg.driver, cfg.connectionUrl, cfg.username, cfg.password)
  val repo = new FeedbackRepository()

  after {
    sql"delete from feedback".update.run.transact(xa).run
  }

  describe("Postgres database") {

    it("should insert a feedback", DatabaseTag) {
      val response: FeedbackId = repo.insertFeedback(FeedbackTestData.createFeedback()).transact(xa).run

      response.id should be > 0
    }

    it ("should query an inserted feedback", DatabaseTag) {
      repo.insertFeedback(FeedbackTestData.createFeedback()).transact(xa).run

      val feedbacks: List[Feedback] = repo.selectFeedbacks().transact(xa).run

      feedbacks should have size 1
    }
  }

}
