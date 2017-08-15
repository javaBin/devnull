package devnull.sessions

import java.util.UUID

import org.scalatest.{FunSpec, Matchers}

class IdSpec extends FunSpec with Matchers {

  describe("EventId") {
    it("should use dash in toString") {
      val uuid = UUID.randomUUID()
      val id = EventId(uuid, true)

      id.toString shouldBe uuid.toString
      id.toString should contain ('-')
    }

    it("should not use dash in toString") {
      val uuid = UUID.randomUUID()
      val id = EventId(uuid, false)

      id.toString should not contain ('-')
    }
  }

}
