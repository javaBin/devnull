package devnull.rest.helpers

import javax.servlet.http.HttpServletRequest

import devnull.rest.dto.FaultResponse
import devnull.rest.helpers.ResponseWrites.ResponseJson
import devnull.storage.VoterInfo
import unfiltered.directives.Directives._
import unfiltered.directives.FilterDirective
import unfiltered.directives.Result.Success
import unfiltered.request.{HttpRequest, StringHeader, UserAgent, XForwardedFor}
import unfiltered.response.{BadRequest, ResponseFunction}

object VoterIdentification {

  private object VoterIdHeader extends StringHeader("Voter-ID")

  def identify() =
    for {
      voterId <- commit(when { case VoterIdHeader(voterId) => voterId }
        .orElse(BadRequest ~> ResponseJson(FaultResponse("parsing", "Missing Voter-ID header"))))
      userAgent <- commit(whenOr { case UserAgent(ua) => ua }.orElse("unknown"))
      ipAddress <- commit(request[HttpServletRequest].map(r => r.remoteAddr))
      forwardFor <- commit(whenOr { case XForwardedFor(ff) => ff.headOption }.orElse(None))
    } yield VoterInfo(voterId, forwardFor.getOrElse(ipAddress), userAgent)

  /* HttpRequest has to be of type Any because of type-inference (SLS 8.5) */
  case class whenOr[A](f: PartialFunction[HttpRequest[Any], A]) {
    def orElse[R](elseValue: A) =
      new FilterDirective[Any, ResponseFunction[R], A](r =>
        if (f.isDefinedAt(r)) Success(f(r)) else Success(elseValue),
        _ => Success(elseValue)
      )
  }

}


