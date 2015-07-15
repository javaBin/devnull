package devnull.rest.helpers

import javax.servlet.http.HttpServletRequest

import net.hamnaberg.json.collection.{NativeJsonCollectionParser, Template}
import unfiltered.directives.Directive
import unfiltered.directives.Directives._
import unfiltered.response.{BadRequest, ResponseFunction, ResponseString}

object EitherDirective {

  type EitherDirective[T] = Directive[HttpServletRequest, ResponseFunction[Any], T]

  def withTemplate[T](fromTemplate: (Template) => T): EitherDirective[Either[Throwable, T]] = {
    for {
      template <- inputStream.map(is => NativeJsonCollectionParser.parseTemplate(is))
    } yield template.right.map(fromTemplate)
  }

  def fromEither[T](either: Either[Throwable, T]): EitherDirective[T] = {
    either.fold(
      ex => failure(BadRequest ~> ResponseString(ex.getMessage)),
      a => success(a)
    )
  }

}
