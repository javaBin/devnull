package devnull.sessions

trait SessionClient {
  def session(eventId: EventId, sessionId: SessionId): Option[Session]
}

