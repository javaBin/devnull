package devnull.sessions

import java.io.{BufferedReader, InputStreamReader}
import java.nio.charset.Charset

import devnull.UuidFromString
import dispatch.{url => reqUrl, _}
import org.asynchttpclient.Response
import org.json4s.JsonAST.{JField, JString}
import org.json4s.native.JsonParser
import org.json4s.{JObject, JValue}

import scala.concurrent.Await
import scala.concurrent.duration.DurationDouble

class SleepingPillHttpSessionClient(val baseUrl: String) extends SessionClient {

  import scala.concurrent.ExecutionContext.Implicits.global

  val c: Http = Http.withConfiguration(_.setFollowRedirect(true))

  override def session(eventId: EventId, sessionId: SessionId): Option[Session] = {
    val pathElements = List("conference", eventId.toString, "session")
    val req: Req = pathElements.reverse.foldRight(reqUrl(baseUrl))((path, re) => re / path)
    val sessionJson = Await.result(c(req OK SessionJson), 15.seconds)
    sessionJson.findSession(sessionId)
  }
}

case class SessionJson(underlying: List[Session]) {
  def findSession(sid: SessionId) = underlying.find(_.sessionId == sid)
}

object SessionJson extends (Response => SessionJson) {
  override def apply(r: Response): SessionJson = {
    val reader = new BufferedReader(new InputStreamReader(r.getResponseBodyAsStream, Charset.forName("UTF-8")))
    val json = JsonParser.parse(reader, closeAutomatically = true, useBigDecimalForDouble = true)
    SessionJson(parse(json))
  }

  def parse(json: JValue): List[Session] = {
    for {
      JObject(sessions) <- json
      JField("sessionId", JString(sid)) <- sessions
      JField("conferenceId", JString(cid)) <- sessions
      JField("startTimeZulu", JString(start)) <- sessions
      JField("endTimeZulu", JString(end)) <- sessions
    } yield Session(
      EventId(UuidFromString(cid).right.get),
      SessionId(UuidFromString(sid).right.get),
      UtcLocalDateTime.parse(start),
      UtcLocalDateTime.parse(end)
    )
  }
}