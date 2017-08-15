package devnull.storage

import java.util.UUID

import devnull.TestTags.DatabaseTag
import doobie.imports.{Update0, Query0}
import doobie.util.transactor.DriverManagerTransactor
import org.scalatest.{Matchers, FunSpec}

import scalaz.concurrent.Task

class FeedbackRepositoryAnalysisSpec extends FunSpec with Matchers with DoobieAnalysisMatcher with DatabaseMigration {

  val cfg = DatabaseConfigEnv()
  implicit val xa = DriverManagerTransactor[Task](cfg.driver, cfg.connectionUrl, cfg.username, cfg.password.value)
  val repo: FeedbackRepository = new FeedbackRepository()

  it("feedback query must match types in the database", DatabaseTag) {
    val query: Query0[Feedback] = repo.Queries.selectAllFeedbacks
    query should matchDatabaseSchemaTypesQuery[Feedback]
  }

  it("feedback insert must match types in the database", DatabaseTag) {
    val insert: Update0 = repo.Queries.insert(FeedbackTestData.createFeedback())

    insert should matchDatabaseSchemaTypesUpdate
  }

  it("feedback session query must match types in the database") {
    val query: Query0[FeedbackResult] = repo.Queries.selectAvgForSession(UUID.randomUUID())
    query should matchDatabaseSchemaTypesQuery[FeedbackResult]
  }

  it("feedback event query must match types in the database") {
    val query: Query0[FeedbackResult] = repo.Queries.selectAvgForEvent(UUID.randomUUID())
    query should matchDatabaseSchemaTypesQuery[FeedbackResult]
  }

  // Does not match the schema (not null) even doe it's filtered out null values.
  ignore("feedback query for comments must match types in the database", DatabaseTag) {
    val query: Query0[String] = repo.Queries.selectComments(UUID.randomUUID())
    query should matchDatabaseSchemaTypesQuery[String]
  }
}
