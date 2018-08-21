import sbt._
import scala.util.Try
import scala.sys.process._

object Version {
  def gitSha = Try { "git rev-parse --short HEAD".!!.trim() }.getOrElse("")

  def apply(base: String) = {
    if (base.endsWith("SNAPSHOT")) base else base + "-" + gitSha
  }
}
