package devnull.sessions

import java.io.{BufferedReader, InputStreamReader}
import java.nio.charset.Charset

import com.typesafe.scalalogging.LazyLogging
import devnull.UuidFromString
import dispatch.{url => reqUrl, _}
import org.asynchttpclient.Response
import org.json4s.JsonAST.{JField, JString}
import org.json4s.native.JsonParser
import org.json4s.{JObject, JValue}

import scala.concurrent.Await
import scala.concurrent.duration.DurationDouble

class SleepingPillHttpSessionClient(val baseUrl: String)
    extends SessionClient
    with LazyLogging {

  import scala.concurrent.ExecutionContext.Implicits.global

  val httpClient: Http = Http.withConfiguration(_.setFollowRedirect(true))

  override def session(eventId: EventId, sessionId: SessionId): Option[Session] = {
    val request =
      (reqUrl(baseUrl) / "public" / "conference" / eventId.toString / "session")
        .setHeader("Accept-Encoding", "gzip")
    try {
      val sessionJson =
        Await.result(httpClient(request OK SessionJson), 15.seconds)
      logger.debug(
        s"Sessions ids from sleeping pill ${sessionJson.underlying.map(_.sessionId)}"
      )
      sessionJson.findSession(sessionId)
    } catch {
      case t: Throwable =>
        logger.warn(s"Failed to execute: ${request.url}", t)
        throw t
    }
  }
}

case class SessionJson(underlying: List[Session]) {
  def findSession(sid: SessionId): Option[Session] = underlying.find(_.sessionId == sid)
}

object SessionJson extends (Response => SessionJson) {
  override def apply(r: Response): SessionJson = {
    val reader = new BufferedReader(
      new InputStreamReader(r.getResponseBodyAsStream, Charset.forName("UTF-8"))
    )
    val json =
      JsonParser.parse(reader, closeAutomatically = true, useBigDecimalForDouble = true)
    SessionJson(parse(json))
  }

  def parse(json: JValue): List[Session] = {
    for {
      JObject(sessions)                       <- json
      JField("sessionId", JString(sid))       <- sessions
      JField("conferenceId", JString(cid))    <- sessions
      JField("startTimeZulu", JString(start)) <- sessions
      JField("endTimeZulu", JString(end))     <- sessions
    } yield
      Session(
        EventId(UuidFromString(cid).right.get),
        SessionId(UuidFromString(sid).right.get),
        UtcLocalDateTime.parse(start),
        UtcLocalDateTime.parse(end)
      )
  }
}
