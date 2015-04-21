package devnull.storage

import java.util.UUID

import scala.util.Random

object FeedbackTestData {

  private val random: Random = new Random()

  def createFeedback(session: UUID = UUID.randomUUID(), source: String = UUID.randomUUID().toString): Feedback = {
    Feedback(null, null, source, session, Ratings(rating().get, rating(), rating(), rating()))
  }

  private def rating() = {
    Some(random.nextInt(5).toShort)
  }

}
