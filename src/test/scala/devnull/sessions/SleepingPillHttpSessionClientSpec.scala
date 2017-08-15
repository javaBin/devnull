package devnull.sessions

import org.json4s.native.JsonMethods.parse
import org.scalatest.{FunSpec, Matchers}

class SleepingPillHttpSessionClientSpec extends FunSpec with Matchers {

  describe("SleepingPillHttpSessionClient") {

    val baseUrl = "https://sleepingpill.javazone.no/public/"
    val eventId = EventId("9f40063a-5f20-4d7b-b1e8-ed0c6cc18a5f")
    val sessionId = SessionId("f04397ac-a3ba-4048-88f0-54a816bb2a84")

    it("should be able to fetch session") {
      val client = new SleepingPillHttpSessionClient(baseUrl)
      val session = client.session(eventId, sessionId)

      session should not be empty
      session.get.eventId should be(eventId)
    }

    it("parse response") {
      val sid = "ed8ef197-f256-443f-b907-34725cf4b038"
      val eid = "3baa25d3-9cca-459a-90d7-9fc349209289"
      val startTime = "2016-09-07T08:20:00Z"
      val endTime = "2016-09-07T09:20:00Z"
      val input = parse(
        s"""{
          | "sessions": [{
          |   "sessionId": "$sid",
          |   "conferenceId": "$eid",
          |   "startTimeZulu": "$startTime",
          |   "endTimeZulu": "$endTime"
          | }]
          |} """.stripMargin)

      val res: List[Session] = SessionJson.parse(input)

      res should have length 1
    }
  }

}
