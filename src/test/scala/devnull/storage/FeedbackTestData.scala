package devnull.storage

import java.util.UUID

import scala.util.Random

object FeedbackTestData {

  private val random: Random = new Random()

  def createFeedback(
      session: UUID = UUID.randomUUID(),
      source: String = UUID.randomUUID().toString,
      voterId: String = "1234"): Feedback = {
    val info = VoterInfo(voterId, "127.0.0.1", "spec")
    val ratings = Ratings(rating().get, rating(), rating(), rating())
    Feedback(null, null, info, session, ratings)
  }

  def createPaperFeedback(eventId: UUID = UUID.randomUUID()) = PaperFeedback(
    null,
    null,
    eventId,
    UUID.randomUUID(),
    PaperRating(1, 2, 3),
    4)

  private def rating() = {
    Some(random.nextInt(5).toShort)
  }

}
