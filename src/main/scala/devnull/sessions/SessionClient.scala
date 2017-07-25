package devnull.sessions

trait SessionClient {
  def session(eventId: EventId, session: SessionId): Option[Session]
}

