package devnull.rest.helpers
import devnull.rest.helpers.DirectiveHelper.{some, when}
import unfiltered.directives.Directives.commit
import unfiltered.request.{HttpRequest, RequestContentType}
import unfiltered.response.UnsupportedMediaType

object ContentTypeResolver {

  def withContentType(ct: String) = commit(when {
    case RequestContentType(`ct`) => ct
  }.orElse(UnsupportedMediaType))

  def validContentType = commit(
    some{
      val json: PartialFunction[HttpRequest[Any], SupportedContentType] = {
        case RequestContentType(JsonContentType.value) => JsonContentType
      }
      val jsonCollection: PartialFunction[HttpRequest[Any], SupportedContentType] = {
        case RequestContentType(CollectionJsonContentType.value) => CollectionJsonContentType
      }
      List(json, jsonCollection
    ) }.orElse(UnsupportedMediaType)
  )
}

sealed abstract class SupportedContentType(val value: String) {
  override def toString: String = value
}

object JsonContentType extends SupportedContentType("application/json")
object CollectionJsonContentType extends SupportedContentType("application/vnd.collection+json")