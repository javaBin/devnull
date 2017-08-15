package devnull.storage

import java.util.UUID

import devnull.TestTags.DatabaseTag
import doobie.imports._
import doobie.util.transactor.DriverManagerTransactor
import org.scalatest.{BeforeAndAfter, Matchers, FunSpec}

import scalaz.concurrent.Task

class PaperFeedbackAnalysisRepositorySpec extends FunSpec with BeforeAndAfter  with Matchers with DoobieAnalysisMatcher with DatabaseMigration  {

  val cfg = DatabaseConfigEnv()
  implicit val xa = DriverManagerTransactor[Task](cfg.driver, cfg.connectionUrl, cfg.username, cfg.password.value)
  val repo: PaperFeedbackRepository = new PaperFeedbackRepository()

  after {
    sql"delete from paper_feedback".update.run.transact(xa).run
  }

  describe("paper feedback") {
    it("query for session must match types in the database", DatabaseTag) {
      val query: Query0[PaperFeedback] = repo.Queries.selectFeedback(UUID.randomUUID())
      query should matchDatabaseSchemaTypesQuery[PaperFeedback]
    }

    it("query for event must match types in the database", DatabaseTag) {
      val query: Query0[(PaperRatingResult, Option[Double])] = repo.Queries.selectAvgFeedbackForEvent(UUID.randomUUID())
      query should matchDatabaseSchemaTypesQuery[(PaperRatingResult, Option[Double])]
    }

    it("insert must match types in the database", DatabaseTag) {
      val insert: Update0 = repo.Queries.insert(FeedbackTestData.createPaperFeedback())

      insert should matchDatabaseSchemaTypesUpdate
    }
  }

}
