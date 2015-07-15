package devnull.rest.helpers

import java.io.OutputStreamWriter

import net.hamnaberg.json.collection.JsonCollection
import org.json4s.native.Serialization
import unfiltered.response.{ComposeResponse, ContentType, ResponseWriter}

object ResponseWrites {

  object ResponseJson {
    val contentType = "application/json"

    implicit val formats = org.json4s.DefaultFormats

    def apply[T <: AnyRef](t: T) = {

      new ComposeResponse[Any](ContentType(contentType)) ~> new ResponseWriter {
        def write(writer: OutputStreamWriter) = {
          Serialization.write(t, writer)
        }
      }
    }
  }

  object ResponseCollectionJson {
    val contentType = "application/vnd.collection+json"

    def apply(coll: JsonCollection) = {
      new ComposeResponse[Any](ContentType(contentType) ~> new ResponseWriter {
        def write(writer: OutputStreamWriter) {
          coll.writeTo(writer)
        }
      })
    }
  }


}
