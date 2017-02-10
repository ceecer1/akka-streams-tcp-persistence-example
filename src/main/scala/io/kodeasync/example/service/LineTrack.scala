package io.kodeasync.example.service

import java.util.UUID

import akka.actor.{ActorSystem, Props}
import io.kodeasync.example.models.readtracker.LineTracker
import org.slf4j.LoggerFactory

/**
  * Created by shishir on 8/5/16.
  */
trait LineTrack {

  val logger = LoggerFactory.getLogger(this.getClass)

  def createEmptyTracker: LineTracker
  def updateTracker(old: LineTracker, fresh: LineTracker): LineTracker

}
