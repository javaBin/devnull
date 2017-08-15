package devnull

import java.util.UUID

import scala.util.{Failure, Success, Try}

object UuidFromString {

  def apply(str: String): Either[Throwable, (UUID, Boolean)] = {
    if (str.contains("-"))  parseStr(str, true)
    else if(str.length == 32) parseStr(List(
      sub(str, 0, 8),
      sub(str, 8, 4),
      sub(str, 12, 4),
      sub(str, 16, 4),
      sub(str, 20, 12)
    ).mkString("-"), false)
    else Left(new IllegalArgumentException("Not a valid UUID format"))
  }

  private def sub(str: String, start: Int, size: Int) =
    str.substring(start, start + size)

  private def parseStr(str: String, containsDash: Boolean) = {
    Try((UUID.fromString(str), containsDash)) match {
      case Success(uuid) => Right(uuid)
      case Failure(t) => Left(t)
    }
  }

}
