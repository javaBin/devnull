package devnull.ems

import java.time.LocalDateTime
import java.util.UUID

sealed trait Id {
  def id: UUID
  override def toString: String = id.toString
}

case class EventId(id: UUID) extends Id
object EventId {
  def apply(id: String): EventId = new EventId(UUID.fromString(id))
}

case class SessionId(id: UUID) extends Id
object SessionId {
  def apply(id: String): SessionId = new SessionId(UUID.fromString(id))
}

case class Session(
    eventId: EventId,
    sessionId: SessionId,
    startTime: LocalDateTime,
    endTime: LocalDateTime)
