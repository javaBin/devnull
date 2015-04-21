package devnull

import org.scalatest.Tag

object TestTags {
  object DatabaseTag extends Tag("devnull.tag.db")
  object SlowTag extends Tag("devnull.tag.slow")
}
