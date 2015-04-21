package devnull.storage

import java.sql.Timestamp
import java.util.UUID


case class Ratings(overall: Short, relevance: Option[Short], content: Option[Short], quality: Option[Short])

case class FeedbackId(id: Int)

case class Feedback(id: FeedbackId,
                    created: Timestamp,
                    source: String,
                    sessionId: UUID,
                    rating: Ratings)

case class DatabaseConfig(driver: String = "org.postgresql.Driver",
                          connectionUrl: String = "jdbc:postgresql:devnull",
                          username: String = "devnull",
                          password: String = "devnull")
