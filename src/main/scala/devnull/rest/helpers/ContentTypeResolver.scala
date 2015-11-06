package devnull.rest.helpers

import devnull.rest.MIMEType
import devnull.rest.helpers.DirectiveHelper.when
import unfiltered.directives.Directives.commit
import unfiltered.request.{HttpRequest, RequestExtractor}
import unfiltered.response.UnsupportedMediaType

object ContentTypeResolver {

  def withContentTypes(ct: List[MIMEType]) = commit(when {
    case RequestContentType(contentType) if ct.exists(m => m.includes(contentType)) => contentType
  }.orElse(UnsupportedMediaType))

}

object RequestContentType extends RequestExtractor[MIMEType] {
  override def unapply[T](req: HttpRequest[T]): Option[MIMEType] =
    req.headers("Content-Type").toList.headOption.flatMap(MIMEType.apply)
}
