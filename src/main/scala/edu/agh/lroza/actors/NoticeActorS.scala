package edu.agh.lroza.actors

import java.util.UUID
import akka.actor.{UntypedChannel, ActorRef, Actor}
import edu.agh.lroza.common.NoticeS
import akka.event.EventHandler

case class GetNotice(token: UUID)

case class ValidatedGetNotice(originalSender: UntypedChannel)

case class UpdateNotice(token: UUID, title: String, message: String)

case class ValidatedTokenUpdateNotice(originalSender: UntypedChannel, title: String, message: String)

case class ValidatedUpdateNotice(originalSender: UntypedChannel, title: String, message: String)

case class DeleteNotice(token: UUID)

case class ValidatedDeleteNotice(originalSender: UntypedChannel)

class NoticeActorS(noticesActor: ActorRef, loginActor: ActorRef, var notice: NoticeS) extends Actor {

  def updateNotice(title: String, message: String) = {
    val oldTitle = notice.title
    notice = NoticeS(title, message)
    noticesActor ! FreeTitle(oldTitle)
    Right(ActorId(self))
  }

  protected def receive = {
    case GetNotice(token) =>
      loginActor ! ValidateToken(token, self.channel, false, ValidatedGetNotice(self.channel))
    case ValidatedGetNotice(originalSender) =>
      originalSender ! Right(notice)
    case UpdateNotice(token, title, message) =>
      EventHandler.debug(this, "notice=" + notice)
      if (title == notice.title) {
        notice = NoticeS(title, message)
        self reply Right(ActorId(self))
      } else {
        loginActor ! ValidateToken(token, self.channel, false, ValidatedTokenUpdateNotice(self.channel, title, message))
      }
    case ValidatedTokenUpdateNotice(originalSender, title, message) =>
      noticesActor ! ReserveTitle(title, originalSender, ValidatedUpdateNotice(originalSender, title, message))
    case ValidatedUpdateNotice(originalSender, title, message) =>
      originalSender ! updateNotice(title, message)
    case DeleteNotice(token) =>
      loginActor ! ValidateToken(token, self.channel, true, ValidatedDeleteNotice(self.channel))
    case ValidatedDeleteNotice(originalSender) =>
      noticesActor ! DeleteId(ActorId(self))
      noticesActor ! FreeTitle(notice.title)
      originalSender ! None
      self.stop()
  }
}