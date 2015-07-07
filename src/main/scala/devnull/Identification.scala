package devnull

import unfiltered.directives.Directives._
import unfiltered.request.StringHeader
import unfiltered.response.BadRequest

sealed trait Identification

case class VoterId(deviceId: String) extends Identification

object Identification {
  def identify() =
    for {
      voterId <- commit(when { case VoterIdHeader(voterId) => voterId }.orElse(BadRequest))
    } yield VoterId(voterId)
}

private object VoterIdHeader extends StringHeader("Voter-ID")
