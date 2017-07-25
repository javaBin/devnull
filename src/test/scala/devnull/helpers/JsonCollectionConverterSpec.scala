package devnull.helpers

import java.util.UUID

import devnull.rest.helpers.JsonCollectionConverter
import devnull.sessions.{EventId, SessionId}
import devnull.storage.{Feedback, VoterInfo}
import net.hamnaberg.json.collection.{Property, Template}
import org.scalatest.{FunSpec, Matchers}

class JsonCollectionConverterSpec extends FunSpec with Matchers {

  describe("Template <> Feedback") {

    it("convert from template to feedback") {
      val template = Template(List(
        Property("overall", 1),
        Property("relevance", 2),
        Property("content", 3),
        Property("quality", 4)
      ))

      val feedback: Option[Feedback] = toFeedbackWithRandomIds(template)

      feedback should not be empty
    }

    it("convert from template to feedback with minimum properties") {
      val template = Template(List(Property("overall", 1)))

      toFeedbackWithRandomIds(template) should not be empty
    }

    it("should extract minimum rating values from template") {
      val template = Template(List(Property("overall", 1)))

      val feedback: Feedback = toFeedbackWithRandomIds(template).get
      feedback.rating.overall should be(1)
      feedback.rating.content shouldBe empty
      feedback.rating.quality shouldBe empty
      feedback.rating.relevance shouldBe empty
    }

    it("should return None if the template is missing required values") {
      val template = Template(List())

      val feedback: Option[Feedback] = toFeedbackWithRandomIds(template)

      feedback shouldBe empty
    }

    def toFeedbackWithRandomIds(template: Template): Option[Feedback] = {
      JsonCollectionConverter.toFeedback(
        template,
        EventId(UUID.randomUUID()),
        SessionId(UUID.randomUUID()),
        VoterInfo("TestVoterId", "1.2.3.4", "spec")
      )
    }
  }

}
