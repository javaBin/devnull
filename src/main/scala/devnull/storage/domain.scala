package devnull.storage

import java.sql.Timestamp
import java.util.UUID

import scala.util.Properties._

case class VoterInfo(
    voterId: String,
    ipAddress: String,
    clientInfo: String)

case class Ratings(
    overall: Short,
    relevance: Option[Short],
    content: Option[Short],
    quality: Option[Short],
    comments: Option[String] = None)

case class FeedbackId(feedbackId: Int)

case class Feedback(
    id: FeedbackId,
    created: Timestamp,
    voterInfo: VoterInfo,
    sessionId: UUID,
    rating: Ratings)

case class PaperFeedback(
    id: FeedbackId,
    created: Timestamp,
    eventId: UUID,
    sessionId: UUID,
    ratings: PaperRating,
    participants: Int)

case class PaperRating(green: Int, yellow: Int, red: Int)
case class PaperRatingResult(green: Option[Double], yellow: Option[Double], red: Option[Double])

case class FeedbackResult(
    overall: Option[Double],
    relevance: Option[Double],
    content: Option[Double],
    quality: Option[Double],
    count: Option[Double]
    )

case class DatabaseConfig(
    driver: String = "org.postgresql.Driver",
    host: String = "localhost",
    port: Int = 5432,
    database: String = "devnull",
    username: String = "devnull",
    password: SecretString = SecretString("devnull")) {
  val connectionUrl = s"jdbc:postgresql://$host:$port/$database"
}

case class SecretString(value: String) {
  override def toString: String = "****"
}

object DatabaseConfigEnv {

  def apply(): DatabaseConfig = {
    DatabaseConfig(
      database = propOrElse("dbName", envOrElse("DB_NAME", "devnull")),
      host = propOrElse("dbHost", envOrElse("DB_HOST", "localhost")),
      port = propOrElse("dbPort", envOrElse("DB_PORT", "5432")).toInt,
      username = propOrElse("dbUsername", envOrElse("DB_USER", "devnull")),
      password = SecretString(propOrElse("dbPassword", envOrElse("DB_PASSWORD", "devnull")))
    )
  }
}
