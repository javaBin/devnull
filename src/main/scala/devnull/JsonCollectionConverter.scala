package devnull

import java.util.UUID

import devnull.storage.{Feedback, Ratings}
import net.hamnaberg.json.collection.Template
import net.hamnaberg.json.collection.data.JavaReflectionData

object JsonCollectionConverter {

  def toFeedback(template: Template, eventId: String, sessionId: String): Option[Feedback] = {
    implicit val formats = org.json4s.DefaultFormats
    implicit val extractor = new JavaReflectionData[Ratings]

    template.unapply[Ratings] match {
      case None => None
      case Some(r) => Some(Feedback(null, null, "http", UUID.fromString(sessionId), r))
    }
  }

}
