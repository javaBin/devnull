package devnull.sessions

import org.scalatest.{FunSpec, Matchers}

class EmsHttpSessionClientSpec extends FunSpec with Matchers {

  val emsHost = "https://test.javazone.no/ems/server/"
  val eventId = EventId("9f40063a-5f20-4d7b-b1e8-ed0c6cc18a5f")
  val sessionId = SessionId("f04397ac-a3ba-4048-88f0-54a816bb2a84")

  describe("Integration") {
    it("should be able to fetch session") {
      val client = new EmsHttpSessionClient(emsHost)
      val session = client.session(eventId, sessionId)

      session should not be empty
      session.get.eventId should be(eventId)
    }

    it("should not fail") {
      val sessionThatDoesNotExist = SessionId("00000000-0000-0000-0000-000000000000")

      val client = new EmsHttpSessionClient(emsHost)
      val session = client.session(eventId, sessionThatDoesNotExist)

      session should be(empty)
    }

  }
}
