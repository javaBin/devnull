package devnull.sessions

import java.time.LocalDateTime
import java.util.UUID

sealed trait Id {
  def id: UUID

  def useDash: Boolean

  override def toString: String =
    if (useDash) id.toString
    else id.toString.replace("-", "")
}

case class EventId(id: UUID, useDash: Boolean = true) extends Id
object EventId {
  def apply(id: String): EventId = new EventId(UUID.fromString(id))
  def apply(t: Tuple2[UUID, Boolean]): EventId = EventId( t._1, t._2)
}

case class SessionId(id: UUID, useDash: Boolean = true) extends Id
object SessionId {
  def apply(id: String): SessionId = new SessionId(UUID.fromString(id))
  def apply(t: Tuple2[UUID, Boolean]): SessionId = SessionId( t._1, t._2)
}

case class Session(
    eventId: EventId,
    sessionId: SessionId,
    startTime: LocalDateTime,
    endTime: LocalDateTime)
