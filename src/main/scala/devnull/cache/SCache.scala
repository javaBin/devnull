package devnull.cache

import java.util.function.{Function => JFunction}

import com.github.benmanes.caffeine.cache.Cache

class SCache[K, V](val cache: Cache[K, V]) {

  def get(key: K, f: K => V): V = {
    cache.get(key, new JFunction[K, V] {
      override def apply(a: K): V = f(a)
    })
  }
}
