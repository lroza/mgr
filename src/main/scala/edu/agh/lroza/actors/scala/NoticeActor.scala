package edu.agh.lroza.actors.scala

import java.util.UUID
import akka.actor.{UntypedChannel, ActorRef, Actor}
import edu.agh.lroza.actors.scala.LoginActor.ValidateToken
import edu.agh.lroza.actors.scala.NoticeActor._
import edu.agh.lroza.actors.scala.NoticesActor.{DeleteId, ReserveTitle, ActorId, FreeTitle}
import edu.agh.lroza.scalacommon.Notice

class NoticeActor(noticesActor: ActorRef, loginActor: ActorRef, var notice: Notice) extends Actor {

  def updateNotice(title: String, message: String) = {
    val oldTitle = notice.title
    notice = Notice(title, message)
    noticesActor ! FreeTitle(oldTitle)
    Right(ActorId(self))
  }

  protected def receive = {
    case GetNotice(token) =>
      loginActor ! ValidateToken(token, self.channel, false, ValidatedGetNotice(self.channel))
    case ValidatedGetNotice(originalSender) =>
      originalSender ! Right(notice)
    case UpdateNotice(token, title, message) =>
      loginActor ! ValidateToken(token, self.channel, false, ValidatedTokenUpdateNotice(self.channel, title, message))
    case ValidatedTokenUpdateNotice(originalSender, title, message) =>
      if (title == notice.title) {
        notice = Notice(title, message)
        originalSender tell Right(ActorId(self))
      } else {
        noticesActor ! ReserveTitle(title, originalSender, ValidatedUpdateNotice(originalSender, title, message))
      }
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

object NoticeActor {

  case class GetNotice(token: UUID)

  case class UpdateNotice(token: UUID, title: String, message: String)

  case class DeleteNotice(token: UUID)

  private case class ValidatedGetNotice(originalSender: UntypedChannel)

  private case class ValidatedTokenUpdateNotice(originalSender: UntypedChannel, title: String, message: String)

  private case class ValidatedUpdateNotice(originalSender: UntypedChannel, title: String, message: String)

  private case class ValidatedDeleteNotice(originalSender: UntypedChannel)

}