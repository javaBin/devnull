package devnull

import linx.Root

object Links {

  val Events = Root / "events"
  val Event = Events / 'eventId

  val Sessions = Event / "sessions"
  val Session = Sessions / 'sessionId

  val Feedbacks = Session
  val Feedback = Feedbacks / 'feedbackId

  val Achievements = Event / "achievements"
  val Achievement = Achievements / 'achievementId

  val Users = "users"
  val User = 'userId

}
