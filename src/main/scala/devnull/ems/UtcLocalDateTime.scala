package devnull.ems

import java.time.{Instant, LocalDateTime, ZoneOffset}

object UtcLocalDateTime {

  def now(): LocalDateTime = LocalDateTime.now(ZoneOffset.UTC)

  def parse(utcTime: String):LocalDateTime  = LocalDateTime.ofInstant(Instant.parse(utcTime), ZoneOffset.UTC)

}

