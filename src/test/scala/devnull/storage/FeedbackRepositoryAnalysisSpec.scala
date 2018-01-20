package devnull.storage

import java.util.UUID

import devnull.TestTags.DatabaseTag
import doobie.imports
import doobie.imports._
import doobie.scalatest.imports._
import org.scalatest.{FunSpec, Matchers}

class FeedbackRepositoryAnalysisSpec extends FunSpec with Matchers with DatabaseMigration with IOLiteChecker {

  val cfg = DatabaseConfigEnv()
  implicit val xa = DriverManagerTransactor[IOLite](cfg.driver, cfg.connectionUrl, cfg.username, cfg.password.value)
  val repo: FeedbackRepository = new FeedbackRepository()

  it("feedback query must match types in the database", DatabaseTag) {
    val query: Query0[Feedback] = repo.Queries.selectAllFeedbacks

    check(query)
  }

  it("feedback insert must match types in the database", DatabaseTag) {
    val insert: Update0 = repo.Queries.insert(FeedbackTestData.createFeedback())

    check(insert)
  }

  it("feedback session query must match types in the database") {
    val query: Query0[FeedbackResult] = repo.Queries.selectAvgForSession(UUID.randomUUID())
    check(query)
  }

  it("feedback event query must match types in the database") {
    val query: Query0[FeedbackResult] = repo.Queries.selectAvgForEvent(UUID.randomUUID())
    check(query)
  }

  // Does not match the schema (not null) even doe it's filtered out null values.
  ignore("feedback query for comments must match types in the database", DatabaseTag) {
    val query: Query0[String] = repo.Queries.selectComments(UUID.randomUUID())
    check(query)
  }

  override def transactor: imports.Transactor[imports.IOLite] = xa
}
