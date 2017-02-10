package io.kodeasync.example.service

import java.util.UUID

import akka.actor.{ActorSystem, Props}
import io.kodeasync.example.models.readtracker.{LineTracker, LineTrackerId}
import io.kodeasync.example.provider.LineTrackerIdProvider

/**
  * Created by shishir on 8/5/16.
  */
class LineTrackerService extends LineTrack {

  override def createEmptyTracker: LineTracker = {
    val tracker = LineTracker(LineTrackerIdProvider.apply(), 0, 0, 0, "progressing")
    tracker
  }

  override def updateTracker(old: LineTracker, fresh: LineTracker): LineTracker = {
    val alphaNumCharCount = old.alphaNumChars + fresh.alphaNumChars
    val readLineNumberCount = old.readLineNumber + 1
    val totalCharsCount = old.totalCharsInLine + fresh.totalCharsInLine
    val newTracker = old.copy(alphaNumChars = alphaNumCharCount, readLineNumber = readLineNumberCount,
      totalCharsInLine = totalCharsCount, taskName = fresh.taskName)
    newTracker
  }

}
