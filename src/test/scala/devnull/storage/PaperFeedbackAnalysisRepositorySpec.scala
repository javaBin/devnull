package devnull.storage

import java.util.UUID

import devnull.TestTags.DatabaseTag
import doobie.imports
import doobie.imports._
import doobie.scalatest._
import org.scalatest.{FunSpec, Matchers}

class PaperFeedbackAnalysisRepositorySpec extends FunSpec  with Matchers with DatabaseMigration with IOLiteChecker {

  val cfg = DatabaseConfigEnv()
  implicit val xa = DriverManagerTransactor[IOLite](cfg.driver, cfg.connectionUrl, cfg.username, cfg.password.value)
  val repo: PaperFeedbackRepository = new PaperFeedbackRepository()

  describe("paper feedback") {
    it("query for session must match types in the database", DatabaseTag) {
      val query: Query0[PaperFeedback] = repo.Queries.selectFeedback(UUID.randomUUID())
      check(query)
    }

    it("query for event must match types in the database", DatabaseTag) {
      val query: Query0[(PaperRatingResult, Option[Double])] = repo.Queries.selectAvgFeedbackForEvent(UUID.randomUUID())
      check(query)
    }

    it("insert must match types in the database", DatabaseTag) {
      val insert: Update0 = repo.Queries.insert(FeedbackTestData.createPaperFeedback())

      check(insert)
    }
  }

  override def transactor: imports.Transactor[imports.IOLite] = xa
}
