package devnull.ems

import java.time.{ZoneOffset, Clock, LocalDateTime}
import java.util.UUID

import devnull.ems.UtcLocalDateTime.now
import org.scalatest.{Matchers, FunSpec}

class CachingEmsServiceSpec extends FunSpec with Matchers {

  describe("feedback registration") {
    val eId: EventId = EventId(UUID.randomUUID())
    val sId: SessionId = SessionId(UUID.randomUUID())
    implicit val fixedClock: Clock = Clock.fixed(LocalDateTime.of(2015, 9, 8, 10, 0, 0).toInstant(ZoneOffset.UTC), ZoneOffset.UTC)

    it("should find session when session and event id is flipped (android bug)") {
      val repository: CachingEmsService = new CachingEmsService(new EmsClient {
        override def session(eventId: EventId, session: SessionId): Option[Session] = 
          Some(Session(eId, sId, now().minusMinutes(30), now().minusMinutes(20)))
      }, 5)
      repository.getSession(EventId(sId.id), SessionId(eId.id)) should not be empty
    }
    
    it("should be open when 20 min has passed the session end time") {
      val repository: CachingEmsService = new CachingEmsService(new EmsClient {
        override def session(eventId: EventId, session: SessionId): Option[Session] = Some(
          Session(eId, sId, now().minusMinutes(30), now().minusMinutes(20)))
      }, 5)
      repository.canRegisterFeedback(eId, sId) should be(true)
    }

    it("should be open when 5 min has passed the session end time") {
      val repository: CachingEmsService = new CachingEmsService(new EmsClient {
        override def session(eventId: EventId, session: SessionId): Option[Session] = Some(
          Session(eId, sId, now().minusMinutes(30), now().minusMinutes(5).minusSeconds(1)))
      }, 5)
      repository.canRegisterFeedback(eId, sId) should be(true)
    }

    it("should be open 4 min before the session end time") {
      val repository: CachingEmsService = new CachingEmsService(new EmsClient {
        override def session(eventId: EventId, session: SessionId): Option[Session] = Some(
          Session(eId, sId, now().minusMinutes(30), now().plusMinutes(4)))
      }, 5)
      repository.canRegisterFeedback(eId, sId) should be(true)
    }

    it("should not be open 6 min before the session end time ") {
      val repository: CachingEmsService = new CachingEmsService(new EmsClient {
        override def session(eventId: EventId, session: SessionId): Option[Session] =
          Some(Session(eId, sId, now().minusMinutes(30), now().plusMinutes(6)))
      }, 5)
      repository.canRegisterFeedback(eId, sId) should be(false)
    }

    it("should not be open when the session endtime is 24 hours in the future") {
      val repository: CachingEmsService = new CachingEmsService(new EmsClient {
        override def session(eventId: EventId, session: SessionId): Option[Session] = Some(
          Session(eId, sId, now().minusMinutes(30), now().plusHours(24)))
      }, 5)
      repository.canRegisterFeedback(eId, sId) should be(false)
    }
  }
}
