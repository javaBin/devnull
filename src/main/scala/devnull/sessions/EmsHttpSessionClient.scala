package devnull.sessions

import java.util.concurrent.ExecutionException

import dispatch.StatusCode
import net.hamnaberg.json.collection.JsonCollection

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success, Try}

class EmsHttpSessionClient(val baseUrl: String) extends SessionClient with HttpClient {

  override def session(eventId: EventId, sessionId: SessionId): Option[Session] = {
    val path: List[Any] = "events" :: eventId :: "sessions" :: sessionId :: Nil
    val reqPath: List[String] = path.map(pe => pe.toString)

    Try(Some(Await.result(request(reqPath), 15.seconds))) match {
      case Success(response) => handle(response, eventId, sessionId)
      case Failure(e) => handleFailure(e, eventId, sessionId)
    }
  }

  def handle(responseOption: Option[JsonCollection], eventId: EventId, sessionId: SessionId): Option[Session] = {
    for {
      response <- responseOption
      item <- response.items.headOption
      slot <- item.links.find(l => l.rel == "slot item")
      prompt <- slot.prompt
      (startTime, endTime) <- prompt.split('+').map(UtcLocalDateTime.parse) match {
        case Array(p1, p2) => Some((p1, p2))
        case _ => None
      }
    } yield Session(eventId, sessionId, startTime, endTime)

  }

  def handleFailure(e: Throwable, eventId: EventId, sessionId: SessionId): Option[Session] = e match {
    case ex: ExecutionException => ex.getCause match {
      case StatusCode(405) => None
      case StatusCode(404) => None
      case _ => throw EmsException(eventId, sessionId, e)
    }
    case _ => throw EmsException(eventId, sessionId, e)
  }

  case class EmsException(eventId: EventId, sessionId: SessionId, t: Throwable)
      extends Exception(s"EventId: '$eventId', SessionId: '$sessionId'", t)

}
