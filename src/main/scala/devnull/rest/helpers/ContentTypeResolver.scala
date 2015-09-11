package devnull.rest.helpers
import devnull.rest.helpers.DirectiveHelper.{some, when}
import unfiltered.directives.Directives.commit
import unfiltered.request.{RequestExtractor, HttpRequest, RequestContentType}
import unfiltered.response.UnsupportedMediaType

object ContentTypeResolver {

  def withContentType(ct: String) = commit(when {
    case RequestContentType(`ct`) => ct
  }.orElse(UnsupportedMediaType))

  def validContentType = commit(
    some{
      val json: PartialFunction[HttpRequest[Any], SupportedContentType] = {
        case CharsetRequestContentType(JsonContentType.value) => JsonContentType
      }
      val jsonCollection: PartialFunction[HttpRequest[Any], SupportedContentType] = {
        case CharsetRequestContentType(CollectionJsonContentType.value) => CollectionJsonContentType
      }
      List(json, jsonCollection
    ) }.orElse(UnsupportedMediaType)
  )
}

object CharsetRequestContentType extends RequestHeader("Content-Type")(StringValueParser)

class RequestHeader[A](val name: String)(parser: Iterator[String] => List[A]) extends RequestExtractor[A] {
  def unapply[T](req: HttpRequest[T]) = parser(req.headers(name)).headOption
  def apply[T](req: HttpRequest[T]) = parser(req.headers(name)).headOption
}

object StringValueParser extends (Iterator[String] => List[String]) {
  def apply(values: Iterator[String]) =
    values.toList.map(v => v.replace("; charset=.*", ""))
}

sealed abstract class SupportedContentType(val value: String) {
  override def toString: String = value
}

object JsonContentType extends SupportedContentType("application/json")
object CollectionJsonContentType extends SupportedContentType("application/vnd.collection+json")