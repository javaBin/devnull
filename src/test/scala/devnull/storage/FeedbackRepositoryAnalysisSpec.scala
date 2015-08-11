package devnull.storage

import devnull.TestTags.DatabaseTag
import doobie.imports.{Update0, Query0}
import doobie.util.transactor.DriverManagerTransactor
import org.scalatest.{Matchers, FunSpec}

import scalaz.concurrent.Task

class FeedbackRepositoryAnalysisSpec extends FunSpec with Matchers with DoobieAnalysisMatcher with DatabaseMigration {

  val cfg = DatabaseConfigEnv()
  implicit val xa = DriverManagerTransactor[Task](cfg.driver, cfg.connectionUrl, cfg.username, cfg.password)
  val repo: FeedbackRepository = new FeedbackRepository()

  it("feedback query must match types in the database", DatabaseTag) {
    val query: Query0[Feedback] = repo.Queries.selectAllFeedbacks
    query should matchDatabaseSchemaTypesQuery[Feedback]
  }

  it("feedback insert must match types in the database", DatabaseTag) {
    val insert: Update0 = repo.Queries.insert(FeedbackTestData.createFeedback())

    insert should matchDatabaseSchemaTypesUpdate
  }
}
