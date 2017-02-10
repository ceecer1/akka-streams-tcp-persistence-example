package io.kodeasync.example.manager

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import io.kodeasync.example.models.Common.{Envelope, GetTracker, GetTrackerSuccess}
import io.kodeasync.example.models.readtracker.{LineTracker, LineTrackerId}
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike}
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._

/**
  * Created by shishir on 8/8/16.
  */
class PersistentManagerResolverTest extends TestKit(ActorSystem("ProgrammingTest", TestConfig.config)) with ImplicitSender with FlatSpecLike with MockitoSugar with BeforeAndAfterAll {


  "A resource manager resolver" should
    "handle a request" in new Base {

    val routeRequest = RouteRequest(lineTrackerId, getTracker)

    actorUnderTest ! routeRequest
    lineManagerProbe.expectMsg(Envelope(lineTrackerId, getTracker))
    lineManagerProbe.reply(GetTrackerSuccess(lineTracker))

  }

  override def afterAll(): Unit = {
    shutdown()
  }


}

abstract class Base(implicit actorSystem: ActorSystem) extends MockitoSugar {

  val actorReference = mock[ActorLookup]
  val actorUnderTest = TestActorRef(new PersistentManagerResolver(actorReference))
  val lineTrackerId = LineTrackerId("lineTrackerId", "lineTrackerShard")
  val getTracker = GetTracker(lineTrackerId)

  val lineTracker = LineTracker(lineTrackerId, 2, 3, 4, "progressing")

  val lineManagerProbe = TestProbe()

  println(s"lineManagerProbe=${lineManagerProbe.testActor}")

  when(actorReference.actorLookup(lineTrackerId)) thenReturn lineManagerProbe.testActor

}

object TestConfig {
  val hostname = "127.0.0.1"
  val port = "3551"
  val systemName = "ProgrammingTest"
  def config = {

    ConfigFactory.parseString(
      s"""
         |akka.loglevel=WARNING
         |akka {
         |  loggers = ["akka.event.slf4j.Slf4jLogger"]
         |}
         |akka.actor.provider="akka.cluster.ClusterActorRefProvider"
         |akka.remote.netty.tcp.hostname="$hostname"
         |akka.remote.netty.tcp.port="$port"
         |akka.cluster.seed-nodes=["akka.tcp://$systemName@$hostname:$port"]
         |akka {
         |  persistence {
         |    journal.plugin = "inmemory-journal"
         |    snapshot-store.plugin = "inmemory-snapshot-store"
         |  }
         |}
         |""".stripMargin)

  }


}