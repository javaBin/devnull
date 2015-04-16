package devnull

import org.scalatest.{FunSpec, Tag}

object TestTags {
  object DatabaseTag extends Tag("devnull.tag.db")
  object SlowTag extends Tag("devnull.tag.slow")
}


trait DevNullSpec extends FunSpec {

  def xdescribe(description: String)(fun: => Unit) = ignore(description) _

}
