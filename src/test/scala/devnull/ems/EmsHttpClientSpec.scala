package devnull.ems

import java.util.UUID

import org.scalatest.{FunSpec, Matchers}

class EmsHttpClientSpec extends FunSpec with Matchers {

  describe("Integration") {
    it("should be able to fetch session") {
      val eId = EventId(UUID.fromString("0e6d98e9-5b06-42e7-b275-6abadb498c81"))
      val sId = SessionId(UUID.fromString("d0e180a3-2aa6-468d-a65f-12859cf1bc66"))

      val client = new EmsHttpClient("http://javazone.no/ems/server/")
      val session = client.session(eId, sId)

      session should not be empty
      session.get.eventId should be(eId)
    }
  }
}
