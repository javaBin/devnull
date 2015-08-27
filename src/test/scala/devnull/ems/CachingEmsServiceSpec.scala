package devnull.ems

import java.util.UUID

import devnull.ems.UtcLocalDateTime.now
import org.scalatest.{Matchers, FunSpec}

class CachingEmsServiceSpec extends FunSpec with Matchers {

  describe("feedback registration") {
    val eId: EventId = EventId(UUID.randomUUID())
    val sId: SessionId = SessionId(UUID.randomUUID())

    it("should be open when 20 min has passed the session end time") {
      val repository: CachingEmsService = new CachingEmsService(new EmsClient {
        override def session(eventId: EventId, session: SessionId): Option[Session] = Some(
          Session(eId, sId, now().minusMinutes(30), now().minusMinutes(20)))
      })
      repository.canRegisterFeedback(eId, sId) should be(true)
    }

    it("should be open when 5 min has passed the session end time") {
      val repository: CachingEmsService = new CachingEmsService(new EmsClient {
        override def session(eventId: EventId, session: SessionId): Option[Session] = Some(
          Session(eId, sId, now().minusMinutes(30), now().minusMinutes(5).minusSeconds(1)))
      })
      repository.canRegisterFeedback(eId, sId) should be(true)
    }

    it("should not be open 4 min before the session end time ") {
      val repository: CachingEmsService = new CachingEmsService(new EmsClient {
        override def session(eventId: EventId, session: SessionId): Option[Session] = Some(
          Session(eId, sId, now().minusMinutes(30), now().minusMinutes(4)))
      })
      repository.canRegisterFeedback(eId, sId) should be(false)
    }

    it("should not be open when the session endtime is in the future") {
      val repository: CachingEmsService = new CachingEmsService(new EmsClient {
        override def session(eventId: EventId, session: SessionId): Option[Session] = Some(
          Session(eId, sId, now().minusMinutes(30), now().plusDays(1)))
      })
      repository.canRegisterFeedback(eId, sId) should be(false)
    }

  }
}
