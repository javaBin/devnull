package devnull.sessions

import java.time.LocalDateTime
import java.util.UUID

sealed trait Id {
  def id: UUID

  def useDash: Boolean

  override def toString: String =
    if (useDash) id.toString
    else id.toString.replace("-", "")

  override def hashCode(): Int = id.hashCode()

  override def equals(other: scala.Any): Boolean = other match {
    case otherId: Id if other.getClass == this.getClass => otherId.id == this.id
    case _                                              => false
  }
}

case class EventId(id: UUID, useDash: Boolean = true) extends Id
object EventId {
  def apply(id: String): EventId               = new EventId(UUID.fromString(id))
  def apply(t: Tuple2[UUID, Boolean]): EventId = EventId(t._1, t._2)
}

case class SessionId(id: UUID, useDash: Boolean = true) extends Id
object SessionId {
  def apply(id: String): SessionId               = new SessionId(UUID.fromString(id))
  def apply(t: Tuple2[UUID, Boolean]): SessionId = SessionId(t._1, t._2)
}

case class Session(
    eventId: EventId,
    sessionId: SessionId,
    startTime: LocalDateTime,
    endTime: LocalDateTime,
    sessionType: SessionType
)

sealed trait SessionType
case object Workshop                   extends SessionType
case object Presentation               extends SessionType
case object LightningTalk              extends SessionType
case class UnknownSession(typ: String) extends SessionType

object SessionType {

  def apply(str: String): SessionType = {
    str match {
      case "workshop"       => Workshop
      case "presentation"   => Presentation
      case "lightning-talk" => LightningTalk
      case other            => UnknownSession(other)
    }
  }

}
