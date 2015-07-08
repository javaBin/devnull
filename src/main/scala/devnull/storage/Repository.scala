package devnull.storage

import doobie.contrib.postgresql.pgtypes._
import doobie.hi
import doobie.imports._
import doobie.util.transactor.DriverManagerTransactor

import scalaz.concurrent.Task

class FeedbackRepository {

  val uuidType = UuidType

  object Queries {
    def insert(fb: Feedback): Update0 = {
      sql"""
       INSERT INTO feedback (
           created,
           client_info,
           voter_id,
           ip_address,
           session_id,
           rating_overall,
           rating_relevance,
           rating_content,
           rating_quality
       ) VALUES (
           current_timestamp,
           ${fb.voterInfo.clientInfo},
           ${fb.voterInfo.voterId},
           ${fb.voterInfo.ipAddress},
           ${fb.sessionId},
           ${fb.rating.overall},
           ${fb.rating.relevance},
           ${fb.rating.content},
           ${fb.rating.quality}
       )""".update
    }

    def selectAllFeedbacks: Query0[Feedback] = {
      sql"""
       SELECT
           id,
           created,
           voter_id,
           ip_address,
           client_info,
           session_id,
           rating_overall,
           rating_relevance,
           rating_content,
           rating_quality
       FROM feedback"""
        .query[(Feedback)]
    }

  }

  def insertFeedback(fb: Feedback): hi.ConnectionIO[FeedbackId] = {
    Queries.insert(fb).withUniqueGeneratedKeys[FeedbackId]("id")
  }

  def selectFeedbacks(): hi.ConnectionIO[List[Feedback]] = {
    Queries.selectAllFeedbacks.list
  }
}

object Storage {

  private val config: DatabaseConfig = new DatabaseConfig()
  val xa = DriverManagerTransactor[Task](config.driver, config.connectionUrl, config.username, config.password)

}
