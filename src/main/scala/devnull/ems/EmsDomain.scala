package devnull.ems

import java.time.LocalDateTime
import java.util.UUID

sealed trait Id {
  def id: UUID

  override def toString: String = id.toString
}

case class EventId(id: UUID) extends Id

case class SessionId(id: UUID) extends Id

case class Session(
    eventId: EventId,
    sessionId: SessionId,
    startTime: LocalDateTime,
    endTime: LocalDateTime)
