package devnull.storage

import doobie.contrib.postgresql.pgtypes._
import doobie.imports._
import doobie.util.transactor.{DriverManagerTransactor, Transactor}

import scalaz.concurrent.Task

class FeedbackRepository(xa: Transactor[Task]) {

  val uuidType = UuidType

  def insertFeedback(fb: Feedback): Boolean = {
    val insertFeedback = sql"""INSERT INTO feedback (created, source, session_id,
          rating_overall, rating_relevance, rating_content, rating_quality)
      VALUES (current_timestamp, ${fb.source}, ${fb.sessionId},
       ${fb.ratingOverall}, ${fb.ratingRelevance}, ${fb.ratingContent}, ${fb.ratingQuality})"""
    insertFeedback.update.run.transact(xa).attemptRun.isLeft
  }

  def selectFeedbacks(): List[Feedback] = {
    sql"""
                   SELECT
                    id,
                    created,
                    source,
                    session_id,
                    rating_overall,
                    rating_relevance,
                    rating_content,
                    rating_quality
                   FROM feedback""".query[(Feedback)]
      .list
      .transact(xa)
      .run
  }
}

object Storage {

  private val config: DatabaseConfig = new DatabaseConfig()
  val xa = DriverManagerTransactor[Task](config.driver, config.connectionUrl, config.username, config.password)

}
