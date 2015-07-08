package devnull

import javax.servlet.http.HttpServletRequest

import devnull.storage.VoterInfo
import unfiltered.directives.Directives._
import unfiltered.directives.FilterDirective
import unfiltered.directives.Result.Success
import unfiltered.request.{UserAgent, HttpRequest, StringHeader}
import unfiltered.response.{BadRequest, ResponseFunction}

sealed trait Identification

object Identification {
  def identify() =
    for {
      voterId <- commit(when { case VoterIdHeader(voterId) => voterId }.orElse(BadRequest))
      ipAddress <- commit(request[HttpServletRequest].map(r => r.remoteAddr))
      userAgent <- commit(whenOr { case UserAgent(ua) => ua }.orElse("unknown"))
    } yield VoterInfo(voterId, ipAddress, userAgent)


  /* HttpRequest has to be of type Any because of type-inference (SLS 8.5) */
  case class whenOr[A](f:PartialFunction[HttpRequest[Any], A]){
    def orElse[R](elseValue: A) =
      new FilterDirective[Any, ResponseFunction[R], A](r =>
        if(f.isDefinedAt(r)) Success(f(r)) else Success(elseValue),
        _ =>  Success(elseValue)
      )
  }


}

private object VoterIdHeader extends StringHeader("Voter-ID")
