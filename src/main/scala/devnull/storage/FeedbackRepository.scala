package devnull.storage

import java.util.UUID

import doobie.postgres.pgtypes._
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
           rating_quality,
           comments
       ) VALUES (
           current_timestamp,
           ${fb.voterInfo.clientInfo},
           ${fb.voterInfo.voterId},
           ${fb.voterInfo.ipAddress},
           ${fb.sessionId},
           ${fb.rating.overall},
           ${fb.rating.relevance},
           ${fb.rating.content},
           ${fb.rating.quality},
           ${fb.rating.comments}
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
           rating_quality,
           comments
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
         count(*)     :: FLOAT   AS counts
        FROM (
        WITH unique_feedbacks AS (
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
        FROM unique_feedbacks uf
        where uf.rk = 1
        ORDER BY uf.session_id
        ) fb
         WHERE session_id = $sessionId
         GROUP BY fb.session_id
         ORDER BY counts DESC
      """.query[FeedbackResult]
    }

    def selectAvgForEvent(eventId: UUID): Query0[FeedbackResult] = {
      sql"""
       SELECT
        avg(fb.over) :: FLOAT   AS overall,
        avg(fb.rele) :: FLOAT   AS relevance,
        avg(fb.cont) :: FLOAT   AS content,
        avg(fb.qual) :: FLOAT   AS quality,
        count(*)     :: FLOAT   AS counts
       FROM (
       WITH unique_feedbacks AS (
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
         JOIN paper_feedback pf USING (session_id)
         WHERE event_id = $eventId
       )
       SELECT uf.*
       FROM unique_feedbacks uf
       WHERE uf.rk = 1
       ORDER BY uf.session_id
       ) fb
      """.query[FeedbackResult]
    }

    def selectComments(sessionId: UUID): Query0[String] = {
      sql"""
       SELECT
         fb.comments
       FROM (
              WITH unique_feedbacks AS (
                  SELECT
                    f.id,
                    f.voter_id,
                    f.session_id       AS session_id,
                    f.comments         AS COMMENTS,
                    row_number()
                    OVER(
                      PARTITION BY f.voter_id, f.session_id
                      ORDER BY f.created DESC
                    ) AS rk
                  FROM feedback f
              )
              SELECT uf.*
              FROM unique_feedbacks uf
              WHERE uf.rk = 1
              ORDER BY uf.session_id
            ) fb
       WHERE fb.session_id = $sessionId
       AND fb.comments IS NOT NULL
      """.query[(String)]
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

  def selectFeedbackForEvent(eventId: UUID): hi.ConnectionIO[Option[FeedbackResult]] = {
    Queries.selectAvgForEvent(eventId).option
  }

  def selectComments(sessionId: UUID): hi.ConnectionIO[List[String]] = {
    Queries.selectComments(sessionId).list
  }
}
