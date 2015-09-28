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
            red,
            participants
        ) VALUES (
            current_timestamp,
            ${fb.eventId},
            ${fb.sessionId},
            ${fb.ratings.green},
            ${fb.ratings.yellow},
            ${fb.ratings.red},
            ${fb.participants}
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
            red,
            participants
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
