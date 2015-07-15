package devnull.rest.helpers

import unfiltered.directives.Directives.{commit, when}
import unfiltered.request.RequestContentType
import unfiltered.response.UnsupportedMediaType

object ContentTypeResolver {

  def withContentType(ct: String) = commit(when {
    case RequestContentType(`ct`) => ct
  }.orElse(UnsupportedMediaType))

}
