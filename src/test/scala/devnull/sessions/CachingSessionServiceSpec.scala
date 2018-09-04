package devnull.sessions

import java.time.{Clock, LocalDateTime, ZoneOffset}
import java.util.UUID

import devnull.sessions.UtcLocalDateTime.now
import org.scalatest.{FunSpec, Matchers}

class CachingSessionServiceSpec extends FunSpec with Matchers {

  describe("feedback registration") {
    val eId: EventId   = EventId(UUID.randomUUID())
    val sId: SessionId = SessionId(UUID.randomUUID())
    implicit val fixedClock: Clock = Clock.fixed(
      LocalDateTime.of(2015, 9, 8, 10, 0, 0).toInstant(ZoneOffset.UTC),
      ZoneOffset.UTC
    )

    it("should find session when session and event id is flipped (android bug)") {
      val repository: CachingSessionService = new CachingSessionService(
        (eventId: EventId, session: SessionId) =>
          Some(
            Session(
              eId,
              sId,
              now().minusMinutes(30),
              now().minusMinutes(20),
              Presentation
            )
        ),
        true,
        5
      )
      repository.getSession(EventId(sId.id), SessionId(eId.id)) should not be empty
    }

    it("should be open when 20 min has passed the session end time") {
      val repository: CachingSessionService = new CachingSessionService(
        (eventId: EventId, session: SessionId) =>
          Some(
            Session(
              eId,
              sId,
              now().minusMinutes(30),
              now().minusMinutes(20),
              Presentation
            )
        ),
        true,
        5
      )
      repository.canRegisterFeedback(eId, sId) should be(true)
    }

    it("should be open when 5 min has passed the session end time") {
      val repository: CachingSessionService = new CachingSessionService(
        (eventId: EventId, session: SessionId) =>
          Some(
            Session(
              eId,
              sId,
              now().minusMinutes(30),
              now().minusMinutes(5).minusSeconds(1),
              Presentation
            )
        ),
        true,
        5
      )
      repository.canRegisterFeedback(eId, sId) should be(true)
    }

    it("should be open 4 min before the session end time") {
      val repository: CachingSessionService = new CachingSessionService(
        (eventId: EventId, session: SessionId) =>
          Some(
            Session(eId, sId, now().minusMinutes(30), now().plusMinutes(4), Presentation)
        ),
        true,
        5
      )
      repository.canRegisterFeedback(eId, sId) should be(true)
    }

    it("should not be open 6 min before the session end time ") {
      val repository: CachingSessionService = new CachingSessionService(
        (eventId: EventId, session: SessionId) =>
          Some(
            Session(eId, sId, now().minusMinutes(30), now().plusMinutes(6), Presentation)
        ),
        true,
        5
      )
      repository.canRegisterFeedback(eId, sId) should be(false)
    }

    it("should not be open when the session endtime is 24 hours in the future") {
      val repository: CachingSessionService = new CachingSessionService(
        (eventId: EventId, session: SessionId) =>
          Some(
            Session(eId, sId, now().minusMinutes(30), now().plusHours(24), Presentation)
        ),
        true,
        5
      )
      repository.canRegisterFeedback(eId, sId) should be(false)
    }

    it("should not open workshop before workshop day") {
      val repository: CachingSessionService = new CachingSessionService(
        (eventId: EventId, session: SessionId) =>
          Some(
            Session(
              eventId = eId,
              sessionId = sId,
              startTime = now().minusDays(1).minusMinutes(30),
              endTime = now().minusDays(1).plusHours(2),
              sessionType = Workshop
            )
        ),
        true,
        5
      )
      repository.canRegisterFeedback(eId, sId) should be(false)
    }

    it("should not enfore session ended") {
      val repository: CachingSessionService = new CachingSessionService(
        (eventId: EventId, session: SessionId) =>
          Some(
            Session(
              eventId = eId,
              sessionId = sId,
              startTime = now().minusDays(1).minusMinutes(30),
              endTime = now().minusDays(1).plusHours(2),
              sessionType = Workshop
            )
        ),
        false,
        5
      )
      repository.canRegisterFeedback(eId, sId) should be(false)
    }

    it("should open workshop on workshop day") {
      val repository: CachingSessionService = new CachingSessionService(
        (eventId: EventId, session: SessionId) =>
          Some(
            Session(
              eventId = eId,
              sessionId = sId,
              startTime = now().minusMinutes(30),
              endTime = now().plusHours(2),
              sessionType = Workshop
            )
        ),
        true,
        5
      )
      repository.canRegisterFeedback(eId, sId) should be(true)
    }

    it("should open workshop on days after the workshop") {
      val repository: CachingSessionService = new CachingSessionService(
        (eventId: EventId, session: SessionId) =>
          Some(
            Session(
              eventId = eId,
              sessionId = sId,
              startTime = now().plusDays(1),
              endTime = now().plusDays(1),
              sessionType = Workshop
            )
        ),
        true,
        5
      )
      repository.canRegisterFeedback(eId, sId) should be(true)
    }

  }
}
