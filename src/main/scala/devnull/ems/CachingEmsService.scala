package devnull.ems

import java.time.Clock

import com.github.benmanes.caffeine.cache.{Cache, Caffeine}
import devnull.cache.CaffeineExtensions

import scala.concurrent.duration._

class CachingEmsService(emsClient: EmsClient, bufferTime: Int = 10)(implicit clock: Clock) extends EmsService with CaffeineExtensions {

  private val cache: Cache[CacheKey, Option[Session]] = Caffeine.newBuilder()
      .expireAfterWrite(2.hours)
      .maximumSize(200)
      .build()

  override def getSession(eventId: EventId, sessionId: SessionId): Option[Session] = {
    cache.get(CacheKey(eventId, sessionId), (ck: CacheKey) => emsClient.session(ck.eventId, ck.sessionId)).orElse(
      cache.get(CacheKey(EventId(sessionId.id), SessionId(eventId.id)), (ck: CacheKey) => emsClient.session(ck.eventId, ck.sessionId))
    )
  }

  case class CacheKey(eventId: EventId, sessionId: SessionId)

  override def canRegisterFeedback(eventId: EventId, sessionId: SessionId): Boolean = {
    getSession(eventId, sessionId) match {
      case Some(s) =>

        UtcLocalDateTime.now().isAfter(s.endTime.minusMinutes(bufferTime))

      case None => false
    }
  }
}
