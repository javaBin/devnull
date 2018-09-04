package devnull

import linx.Root

object Links {

  val AppRoot = Root
  val Events  = Root / "events"
  val Event   = Events / 'eventId

  val Sessions = Event / "sessions"
  val Session  = Sessions / 'sessionId

  val Feedbacks = Session / "feedbacks"
  val Feedback  = Feedbacks / 'feedbackId

  val AppInfo = Root / "app-info"
}
