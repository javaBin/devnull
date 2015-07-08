package devnull

import javax.servlet.http.HttpServletRequest

import devnull.storage.VoterInfo
import unfiltered.directives.Directives._
import unfiltered.request.StringHeader
import unfiltered.response.BadRequest

sealed trait Identification

object Identification {
  def identify() =
    for {
      voterId <- commit(when { case VoterIdHeader(voterId) => voterId }.orElse(BadRequest))
      ipAddress <- commit(request[HttpServletRequest].map(r => r.remoteAddr))
    } yield VoterInfo(voterId, ipAddress, "unknown")
}

private object VoterIdHeader extends StringHeader("Voter-ID")
