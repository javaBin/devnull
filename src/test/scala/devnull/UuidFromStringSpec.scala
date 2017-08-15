package devnull

import java.util.UUID

import org.scalatest.{FunSpec, Inside, Matchers}

class UuidFromStringSpec extends FunSpec with Matchers with Inside {

  describe("UuidFromString") {
    it("should parse valid UUID") {
      val uuid = UUID.randomUUID()
      val res = UuidFromString(uuid.toString)

      inside(res) {
        case Right((v, true)) => v shouldBe uuid
      }
    }

    it("should parse UUID without -") {
      val uuid = UUID.randomUUID()
      val res = UuidFromString(uuid.toString.replace("-", ""))

      inside(res) {
        case Right((v, false)) => v shouldBe uuid
      }
    }

    it("should return a left projection when the foramt is invalid ") {
      val uuid = UUID.randomUUID()
      val res = UuidFromString(uuid.toString.replace("-", "/"))

      inside(res) {
        case Left(msg) => msg.getMessage shouldBe "Not a valid UUID format"
      }
    }
  }

}
