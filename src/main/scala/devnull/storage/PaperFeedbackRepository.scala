package devnull.storage

import java.util.UUID

import doobie.contrib.postgresql.pgtypes._
import doobie.hi
import doobie.imports._

class PaperFeedbackRepository {

  val uuidType = UuidType

  object Queries {
    def insert(fb: PaperFeedback): Update0 = {
      sql"""
        INSERT INTO paper_feedback (
            created,
            event_id,
            session_id,
            green,
            yellow,
            red
        ) VALUES (
            current_timestamp,
            ${fb.eventId},
            ${fb.sessionId},
            ${fb.green},
            ${fb.yellow},
            ${fb.red}
        )
      """.update
    }

    def selectFeedback(sessionId: UUID): Query0[PaperFeedback] = {
      sql"""
        SELECT
            id,
            created,
            event_id,
            session_id,
            green,
            yellow,
            red
        FROM paper_feedback
        WHERE session_id = $sessionId
      """.query[PaperFeedback]
    }
  }

  def insertPaperFeedback(fb: PaperFeedback): hi.ConnectionIO[FeedbackId] = {
    Queries.insert(fb).withUniqueGeneratedKeys[FeedbackId]("id")
  }

  def selectFeedbackForSession(sessionId: UUID): hi.ConnectionIO[Option[PaperFeedback]] = {
    Queries.selectFeedback(sessionId).option
  }

}
