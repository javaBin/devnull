package devnull.storage

import java.sql.Timestamp
import java.util.UUID

case class VoterInfo(
    voterId: String,
    ipAddress: String,
    clientInfo: String)

case class Ratings(overall: Short, relevance: Option[Short], content: Option[Short], quality: Option[Short])

case class FeedbackId(feedbackId: Int)

case class Feedback(id: FeedbackId,
                    created: Timestamp,
                    voterInfo: VoterInfo,
                    sessionId: UUID,
                    rating: Ratings)

case class DatabaseConfig(driver: String = "org.postgresql.Driver",
                          host: String = "localhost",
                          port: Int = 5432,
                          database: String = "devnull",
                          username: String = "devnull",
                          password: String = "devnull") {
  val connectionUrl = s"jdbc:postgresql://$host:$port/$database"
}
