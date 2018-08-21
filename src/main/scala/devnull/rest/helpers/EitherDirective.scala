package devnull.rest.helpers

import javax.servlet.http.HttpServletRequest

import devnull.rest.dto.FaultResponse
import devnull.rest.helpers.ResponseWrites.ResponseJson
import org.json4s.{JValue, StreamInput}
import org.json4s.native.JsonMethods
import unfiltered.directives.Directive
import unfiltered.directives.Directives._
import unfiltered.response.{BadRequest, ResponseFunction}

object EitherDirective {

  type EitherDirective[T] = Directive[HttpServletRequest, ResponseFunction[Any], T]
  implicit val formats = org.json4s.DefaultFormats

  def withJson[T, P : Manifest](t: P => T):EitherDirective[Either[Throwable, Option[T]]] = {
    inputStream.map(is => {
      implicit val formats = org.json4s.DefaultFormats
      val parse: JValue = JsonMethods.parse(new StreamInput(is))
      Right(Some(t(parse.extract[P])))
    })
  }


  def fromEither[T](either: Either[Throwable, T]): EitherDirective[T] = {
    either.fold(
      ex => failure(BadRequest ~> ResponseJson(FaultResponse(ex))),
      a => success(a)
    )
  }

}
