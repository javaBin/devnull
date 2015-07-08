package devnull.storage

import java.util.UUID

import scala.util.Random

object FeedbackTestData {

  private val random: Random = new Random()

  def createFeedback(session: UUID = UUID.randomUUID(), source: String = UUID.randomUUID().toString): Feedback = {
    val info = VoterInfo("1234", "127.0.0.1", "spec")
    val ratings = Ratings(rating().get, rating(), rating(), rating())
    Feedback(null, null, info, session, ratings)
  }

  private def rating() = {
    Some(random.nextInt(5).toShort)
  }

}
