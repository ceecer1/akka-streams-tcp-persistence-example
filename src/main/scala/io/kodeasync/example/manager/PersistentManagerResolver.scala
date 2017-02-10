package io.kodeasync.example.manager

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.util.Timeout
import io.kodeasync.example.models.Common.{Envelope, ID, Request}
import io.kodeasync.example.models.readtracker.LineTrackerId
import io.kodeasync.example.util.ShardDefinitions

import scala.concurrent.duration._

/**
  * Created by shishir on 8/5/16.
  */
object PersistentManagerResolver {

  def props(implicit actorSystem: ActorSystem): Props = {
    Props(new PersistentManagerResolver(new ActorLookup()))
  }

}

class PersistentManagerResolver(actorReference: ActorLookup) extends Actor with ActorLogging {


  implicit val ec = context.dispatcher
  implicit val timeout = Timeout(5 seconds)
  implicit val actorSystem = context.system


  def receive = {

    case envelope: Envelope =>

      val targetActor = actorReference.actorLookup(envelope.actorId)
      targetActor forward envelope

    case request: RouteRequest =>
      val targetId = request.id
      val targetActor: ActorRef = actorReference.actorLookup(targetId)
      val targetRequest = request.request
      val message = Envelope(targetId, targetRequest)
      targetActor.tell(message, sender)

  }

}

class ActorLookup(implicit actorSystem: ActorSystem) {

  val shards = ShardDefinitions.shards
  val lineManager = shards.lineManager.shardStart


  def actorLookup(id: ID): ActorRef = {
    id match {
      case lineTracker: LineTrackerId => lineManager
    }
  }

}

case class RouteRequest(id: ID, request: Request)