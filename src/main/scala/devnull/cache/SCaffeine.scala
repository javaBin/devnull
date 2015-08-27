package devnull.cache

import java.util.concurrent.TimeUnit

import com.github.benmanes.caffeine.cache.Caffeine

import scala.concurrent.duration.Duration

class SCaffeine[K, V](val builder: Caffeine[K, V]) {

  def expireAfterWrite(d: Duration): Caffeine[K, V] = {
    builder.expireAfterWrite(d.toMillis, TimeUnit.MILLISECONDS)
  }

  def expireAfterAccess(d: Duration): Caffeine[K, V] = {
    builder.expireAfterAccess(d.toMillis, TimeUnit.MILLISECONDS)
  }

}



