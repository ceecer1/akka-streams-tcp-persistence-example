package io.kodeasync.example.handler

import akka.actor.{Actor, ActorLogging, Props}
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorSubscriberMessage.{OnComplete, OnError}
import io.kodeasync.example.handler.EventActorPublisher.Message
import scala.collection.mutable

/**
  * Created by shishir on 8/4/16.
  */
class EventActorPublisher extends Actor with ActorPublisher[Message] with ActorLogging {

  import akka.stream.actor.ActorPublisherMessage._

  var queue: scala.collection.mutable.Queue[Message] = mutable.Queue.empty

  def receive = {
    case m: String =>
      log.info(s"EventActorPublisher - message received and queued: ${m.toString}")
      queue.enqueue(Message(m))
      publish()

    case Request => publish()

    case Cancel =>
      log.info("EventActorPublisher - cancel message received")
      context.stop(self)

    case OnError(err: Exception) =>
      log.info("EventActorPublisher - error message received")
      onError(err)
      context.stop(self)

    case OnComplete =>
      log.info("EventActorPublisher - onComplete message received")
      onComplete()
      context.stop(self)
  }

  def publish() = {
    while (queue.nonEmpty && isActive && totalDemand > 0) {
      log.info("EventActorPublisher - message published")
      onNext(queue.dequeue())
    }
  }
}

object EventActorPublisher {
  case class Message(m: String)
  def props = Props[EventActorPublisher]
}