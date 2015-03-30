package devnull.storage

import java.util.{Date, UUID}

case class Feedback(id: Option[Int],
                    created: Date,
                    source: String,
                    sessionId: UUID,
                    ratingOverall: Int,
                    ratingRelevance: Option[Int],
                    ratingContent: Option[Int],
                    ratingQuality: Option[Int])

case class DatabaseConfig(driver: String = "org.postgresql.Driver",
                          connectionUrl: String = "jdbc:postgresql:devnull",
                          username: String = "devnull",
                          password: String = "devnull")
