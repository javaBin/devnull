package devnull.cache

import com.github.benmanes.caffeine.cache.{Cache, Caffeine}

trait CaffeineExtensions {
  implicit def sCaffeine[K, V](c: Caffeine[K, V]): SCaffeine[K, V] = new SCaffeine[K, V](c)

  implicit def sCache[K, V](c: Cache[K, V]): SCache[K, V] = new SCache[K, V](c)
}
