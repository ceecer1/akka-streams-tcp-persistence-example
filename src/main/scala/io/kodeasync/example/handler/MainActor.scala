package io.kodeasync.example.handler

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import io.kodeasync.example.models.Common._
import io.kodeasync.example.models.readtracker.{LineTracker, LineTrackerId}
import io.kodeasync.example.handler.MainActor.{GetPersistence, Start, UpdatePersistence}
import io.kodeasync.example.manager.{PersistentManagerResolver, RouteRequest}
import io.kodeasync.example.provider.LineTrackerIdProvider
import io.kodeasync.example.util.LineStats

/**
  * Created by shishir on 8/7/16.
  */
class MainActor(implicit actorSystem: ActorSystem) extends Actor with ActorLogging {

  val actorRef = context.actorOf(PersistentManagerResolver.props)

  def receive = {
    case Start => {
      val message = RouteRequest(LineTrackerIdProvider.apply(), CreateNewTracker)
      actorRef ! message
    }

    case stats: UpdatePersistence => {
      val tracker = new LineTracker(LineTrackerIdProvider.apply(), 0, stats.lineStats.alphaChars, stats.lineStats.totalChars, "progressing")
      val message = RouteRequest(LineTrackerIdProvider.apply(), UpdateTracker(tracker))
      actorRef ! message
    }

    case GetPersistence => {
      val message = RouteRequest(LineTrackerIdProvider.apply(), GetTracker(LineTrackerIdProvider.apply()))
      actorRef ! message

    }

    case g: GetTrackerSuccess => {
      val totalAlphaNumPercentage = (g.tracker.alphaNumChars.toDouble / g.tracker.totalCharsInLine) * 100.0
      val averageCharsPerLine = g.tracker.alphaNumChars / g.tracker.readLineNumber
      println("#####################   RESULTS   #############################")
      println(s"Alphanumerics : $totalAlphaNumPercentage %, Average line length: $averageCharsPerLine")
    }

    case c: CreateTrackerSuccess => println(s"Empty persistence resource $c created")
    case u: UpdateTrackerSuccess => println(s"Resource $u updated")
    case e: GetTrackerError => println(e.message.error)
  }
}

object MainActor {
  case object Start
  case class UpdatePersistence(lineStats: LineStats)
  case object GetPersistence

  def props(implicit actorSystem: ActorSystem) = Props(new MainActor)
}
