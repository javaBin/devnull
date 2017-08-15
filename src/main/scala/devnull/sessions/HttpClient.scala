package devnull.sessions

import org.asynchttpclient.Response
import dispatch.{url => reqUrl, _}
import net.hamnaberg.json.collection.JsonCollection

trait HttpClient {

  import scala.concurrent.ExecutionContext.Implicits.global

  val c: Http = Http.withConfiguration(_.setFollowRedirect(true))

  def baseUrl: String

  def request(pathElements: List[String]): Future[JsonCollection] = {
    val req: Req = pathElements.reverse.foldRight(reqUrl(baseUrl))((path, re) => re / path)
    c(req OK Collection)
  }

}

object Collection extends (Response => JsonCollection) {
  override def apply(r: Response): JsonCollection = {
    val parsed: Either[Throwable, JsonCollection] = JsonCollection.parse(r.getResponseBodyAsStream)
    parsed.right.get
  }
}
