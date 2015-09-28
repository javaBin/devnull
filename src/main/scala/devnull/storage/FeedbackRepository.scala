package devnull.storage

import java.util.UUID

import doobie.contrib.postgresql.pgtypes._
import doobie.hi
import doobie.imports._

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

    def selectAvgForSession(sessionId: UUID): Query0[FeedbackResult] = {
      sql"""
        SELECT
         avg(fb.over) :: FLOAT   AS overall,
         avg(fb.rele) :: FLOAT   AS relevance,
         avg(fb.cont) :: FLOAT   AS content,
         avg(fb.qual) :: FLOAT   AS quality,
         count(*)     :: INTEGER AS counts
        FROM (
        WITH uniquie_feedbacks AS (
          SELECT
            f.id,
            f.voter_id,
            substring(f.client_info FROM 0 FOR 30),
            f.session_id       AS session_id,
            f.rating_overall   AS over,
            f.rating_relevance AS rele,
            f.rating_content   AS cont,
            f.rating_quality   AS qual,
            row_number()
              OVER(
                PARTITION BY f.voter_id, f.session_id
                ORDER BY f.created DESC
              ) AS rk
          FROM feedback f
        )
        SELECT uf.*
        FROM uniquie_feedbacks uf
        where uf.rk = 1
        ORDER BY uf.session_id
        ) fb
         WHERE session_id = $sessionId
         GROUP BY fb.session_id
         ORDER BY counts DESC
      """.query[FeedbackResult]
    }

  }

  def insertFeedback(fb: Feedback): hi.ConnectionIO[FeedbackId] = {
    Queries.insert(fb).withUniqueGeneratedKeys[FeedbackId]("id")
  }

  def selectFeedbacks(): hi.ConnectionIO[List[Feedback]] = {
    Queries.selectAllFeedbacks.list
  }

  def selectFeedbackForSession(sessionId: UUID): hi.ConnectionIO[Option[FeedbackResult]] = {
    Queries.selectAvgForSession(sessionId).option
  }

}
