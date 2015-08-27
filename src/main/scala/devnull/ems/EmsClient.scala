package devnull.ems

import dispatch.Future

import scala.concurrent.Await
import scala.concurrent.duration._

trait EmsClient {
  def session(eventId: EventId, session: SessionId): Option[Session]
}

class EmsHttpClient(val baseUrl: String) extends EmsClient with HttpClient {

  override def session(eventId: EventId, sessionId: SessionId): Option[Session] = {
    val path: List[Any] = "events" :: eventId :: "sessions" :: sessionId :: Nil
    val reqPath: List[String] = path.map(pe => pe.toString)
    println(reqPath)
    for {
      response <- Some(Await.result(request(reqPath), 15.seconds))
      item <- response.items.headOption
      slot <- item.links.find(l => l.rel == "slot item")
      prompt <- slot.prompt
      (startTime, endTime) <- prompt.split('+').map(UtcLocalDateTime.parse) match {
        case Array(p1, p2) => Some((p1, p2))
        case _ => None
      }
    } yield Session(eventId, sessionId, startTime, endTime)
  }

}