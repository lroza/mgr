package edu.agh.lroza.actors

import java.util.UUID
import akka.actor.{UntypedChannel, ActorRef, Actor}
import edu.agh.lroza.common.{NoticeS, Id, ProblemS}
import akka.event.EventHandler

case class ListNoticesIds(token: UUID)

case class ValidatedListNoticesIds(originalSender: UntypedChannel)

case class AddNotice(token: UUID, title: String, message: String)

case class ValidatedAddNotice(originalSender: UntypedChannel, title: String, message: String)

case class ActorId(actor: ActorRef) extends Id

case class ReserveTitle(title: String, originalSender: UntypedChannel, returnMessage: AnyRef)

case class FreeTitle(title: String)

case class DeleteId(id: ActorId)

class NoticesActorS(loginActor: ActorRef) extends Actor {
  var titles = Set[String]()
  var ids = Set[Id]()

  def listNoticesIds = Right(ids)

  def addNotice(title: String, message: String) = {
    if (titles.contains(title)) {
      Left(ProblemS("Topic with title '" + title + "' already exists"))
    } else {
      titles = titles + title;
      val actorId = new ActorId(Actor.actorOf(new NoticeActorS(self, loginActor, NoticeS(title, message))).start())
      ids = ids + actorId
      Right(actorId)
    }
  }

  protected def receive = {
    case ListNoticesIds(token) =>
      loginActor ! ValidateToken(token, self.channel, false, ValidatedListNoticesIds(self.channel))
    case ValidatedListNoticesIds(originalSender) =>
      originalSender ! listNoticesIds
    case AddNotice(token, title, message) =>
      loginActor ! ValidateToken(token, self.channel, false, ValidatedAddNotice(self.channel, title, message))
    case ValidatedAddNotice(originalSender, title, message) =>
      originalSender ! addNotice(title, message)
    case ReserveTitle(title, originalSender, message) =>
      EventHandler.debug(this, "titles=" + titles + "; title=" + title)
      if (titles.contains(title)) {
        originalSender ! Left(ProblemS("Topic with title '" + title + "' already exists"))
      } else {
        titles = titles + title
        self reply message
      }
    case FreeTitle(title) =>
      titles = titles - title
    case DeleteId(id) =>
      ids = ids - id
  }
}