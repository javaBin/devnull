package devnull.sessions

trait SessionService {

  def getSession(eventId: EventId, sessionId: SessionId): Option[Session]
  def canRegisterFeedback(eventId: EventId, sessionId: SessionId): Boolean

}


