package devnull.sessions

import org.scalatest.{FunSpec, Matchers}

class UtcLocalDateTimeSpec extends FunSpec with Matchers {

  describe("UtcLocalDateTime") {
    it("should parse utc time") {
      val res = UtcLocalDateTime.parse("2016-09-08T07:12:13Z")

      res.getYear shouldBe 2016
      res.getMonthValue shouldBe 9
      res.getDayOfMonth shouldBe 8
      res.getHour shouldBe 7
      res.getMinute shouldBe 12
      res.getSecond shouldBe 13
    }
  }

}
