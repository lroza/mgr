package edu.agh.lroza.common

import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.scalatest.matchers.ShouldMatchers
import org.mockito.Mockito._
import akka.actor.{Actor, ActorRef}
import java.util.UUID

class ServerClientTest extends FunSuite with MockitoSugar with BeforeAndAfter with ShouldMatchers {
  val server = mock[Server]
  val serverActor: ActorRef = Actor.actorOf(ServerActor(server)).start()
  val serverClient = new ServerClient(serverActor)
  val uuid = UUID.randomUUID()

  before {
    reset(server)
  }

  test("login should call server object") {
    when(server.login("a", "b")).thenReturn(Some(uuid))
    val uuid1 = serverClient.login("a", "b")
    uuid1.get should equal(uuid)
    verify(server).login("a", "b")
  }

  test("logout should call server object") {
    when(server.logout(uuid)).thenReturn(true)
    serverClient.logout(uuid) should equal(true)
    verify(server).logout(uuid)
  }

  test("list should call server object") {
    when(server.listTopics(null)).thenReturn(Right(Iterable[String]()))
    serverClient.listTopics(null) should equal(Right(Iterable[String]()))
    verify(server).listTopics(null)
  }

  test("addTopic should call server object") {
    val topic = Right(Topic(""))
    when(server.addTopic(null, null, null)).thenReturn(topic)
    serverClient.addTopic(null, null, null) should equal(topic)
    verify(server).addTopic(null, null, null)
  }

  test("getTopic should call server object") {
    val topic = Right(Topic(""))
    when(server.getTopic(null, null)).thenReturn(topic)
    serverClient.getTopic(null, null) should equal(topic)
    verify(server).getTopic(null, null)
  }

  test("updateTopic should call server object") {
    val topic = Right(Topic(""))
    when(server.updateTopicTitle(null, null, null)).thenReturn(topic)
    serverClient.updateTopicTitle(null, null, null) should equal(topic)
    verify(server).updateTopicTitle(null, null, null)
  }
}