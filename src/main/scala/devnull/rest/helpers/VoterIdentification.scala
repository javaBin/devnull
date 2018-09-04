package devnull.rest.helpers

import javax.servlet.http.HttpServletRequest

import devnull.rest.dto.FaultResponse
import devnull.rest.helpers.DirectiveHelper.when
import devnull.rest.helpers.ResponseWrites.ResponseJson
import devnull.storage.VoterInfo
import unfiltered.directives.Directives._
import unfiltered.request.{StringHeader, UserAgent}
import unfiltered.response.BadRequest

object VoterIdentification {

  private object VoterIdHeader extends StringHeader("Voter-ID")

  def identify() =
    for {
      voterId <- commit(
                  when { case VoterIdHeader(voterId) => voterId }.orElse(
                    BadRequest ~> ResponseJson(
                      FaultResponse("parsing", "Missing Voter-ID header")
                    )
                  )
                )
      userAgent <- commit(when { case UserAgent(ua) => ua }.orElse("unknown"))
      ipAddress <- commit(request[HttpServletRequest].map(r => r.remoteAddr))
      originIp  <- commit(when { case XRealIP(ff) => ff }.orElse(ipAddress))
    } yield {
      VoterInfo(
        voterId,
        originIp,
        userAgent.substring(0, Math.min(userAgent.length, 300))
      )
    }

  object XRealIP extends StringHeader("X-Real-IP")

}
