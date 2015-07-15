package devnull.rest.helpers

import java.util.UUID

import devnull.storage.{Feedback, Ratings, VoterInfo}
import net.hamnaberg.json.collection.Template
import net.hamnaberg.json.collection.data.JavaReflectionData

object JsonCollectionConverter {

  def toFeedback(template: Template, eventId: String, sessionId: String, voterInfo: VoterInfo): Option[Feedback] = {
    implicit val formats = org.json4s.DefaultFormats
    implicit val extractor = new JavaReflectionData[Ratings]

    template.unapply[Ratings] match {
      case None => None
      case Some(r) => Some(Feedback(null, null, voterInfo, UUID.fromString(sessionId), r))
    }
  }

}
