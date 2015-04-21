package devnull.storage

import java.sql.Timestamp
import java.util.UUID


case class FeedbackId(id: Int)
case class Feedback(id: FeedbackId,
                    created: Timestamp,
                    source: String,
                    sessionId: UUID,
                    ratingOverall: Short,
                    ratingRelevance: Option[Short],
                    ratingContent: Option[Short],
                    ratingQuality: Option[Short])

case class DatabaseConfig(driver: String = "org.postgresql.Driver",
                          connectionUrl: String = "jdbc:postgresql:devnull",
                          username: String = "devnull",
                          password: String = "devnull")
