package devnull.ems

trait EmsService {

  def getSession(eventId: EventId, sessionId: SessionId): Option[Session]
  def canRegisterFeedback(eventId: EventId, sessionId: SessionId): Boolean

}


