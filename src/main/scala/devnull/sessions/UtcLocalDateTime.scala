package devnull.sessions

import java.time._

object UtcLocalDateTime {

  def now()(implicit clock: Clock): LocalDateTime =
    LocalDateTime.now(clock)

  def today()(implicit clock: Clock): LocalDate =
    LocalDate.now(clock)

  def parse(utcTime: String): LocalDateTime =
    LocalDateTime.ofInstant(Instant.parse(utcTime), ZoneOffset.UTC)

}
