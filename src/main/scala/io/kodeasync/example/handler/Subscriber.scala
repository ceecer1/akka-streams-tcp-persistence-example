package io.kodeasync.example.handler

import akka.actor.{ActorLogging, ActorSystem, Props}
import akka.stream.actor.{ActorSubscriber, OneByOneRequestStrategy}
import akka.stream.actor.ActorPublisherMessage.{Cancel, Request}
import akka.stream.actor.ActorSubscriberMessage.{OnComplete, OnError, OnNext}
import io.kodeasync.example.handler.MainActor.{GetPersistence, UpdatePersistence}
import io.kodeasync.example.util.LineStats

/**
  * Created by shishir on 8/4/16.
  */
class Subscriber extends ActorSubscriber with ActorLogging {

  val requestStrategy = OneByOneRequestStrategy

  val main = context.actorOf(MainActor.props(context.system))

  def receive = {
    case OnNext(stat: LineStats) => {
      log.info(s"Received Line stats : aplha chars ${stat.alphaChars} total chars ${stat.totalChars}")
      main ! UpdatePersistence(stat)
    }

    case OnComplete => {
      log.info("Data Stream Completed")
      main ! GetPersistence
      context.system.terminate()
    }
    case OnError(err) => log.error(err, "Data Stream Error")
    case _ =>
  }

}

object Subscriber {
  case class Stat(totalCharPercent: Double, lineCharCount: Int, lineNumber: Long)
  def props = Props[Subscriber]
}