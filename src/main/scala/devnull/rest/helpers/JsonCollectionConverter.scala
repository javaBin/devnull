package devnull.rest.helpers

import devnull.sessions.{EventId, SessionId}
import devnull.storage.{Feedback, Ratings, VoterInfo}
import net.hamnaberg.json.collection.Template
import net.hamnaberg.json.collection.data.JavaReflectionData

object JsonCollectionConverter {

  def toFeedback(template: Template, eventId: EventId, sessionId: SessionId, voterInfo: VoterInfo): Option[Feedback] = {
    implicit val formats = org.json4s.DefaultFormats
    implicit val extractor = new JavaReflectionData[Ratings]

    template.unapply[Ratings] match {
      case None => None
      case Some(r) => Some(Feedback(null, null, voterInfo, sessionId.id, r))
    }
  }

}
