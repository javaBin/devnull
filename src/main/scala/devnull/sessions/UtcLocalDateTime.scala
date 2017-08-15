package devnull.sessions

import java.time.{Clock, Instant, LocalDateTime, ZoneOffset}

object UtcLocalDateTime {

  def now()(implicit clock: Clock): LocalDateTime =
    LocalDateTime.now(clock)

  def parse(utcTime: String): LocalDateTime =
    LocalDateTime.ofInstant(Instant.parse(utcTime), ZoneOffset.UTC)

}

