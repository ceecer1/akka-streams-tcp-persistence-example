package io.kodeasync.example.manager.service

import io.kodeasync.example.models.readtracker.{LineTracker, LineTrackerId}
import io.kodeasync.example.service.LineTrackerService
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike}
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._

/**
  * Created by shishir on 8/8/16.
  */
class LineTrackerServiceTest extends FlatSpecLike with MockitoSugar with BeforeAndAfterAll {

  "A tracker service " should
    "create an empty tracker" in new Base {

    val tracker = service.createEmptyTracker

    assert(tracker.alphaNumChars == 3)

  }

  "Service " should
    "update an old tracker" in new Base {

    val tracker = service.updateTracker(oldTracker, lineTracker)

    assert(tracker.alphaNumChars == 11)

  }
}

abstract class Base extends MockitoSugar {

  val lineTrackerId = LineTrackerId("lineTrackerId", "lineTrackerShard")

  val lineTracker = LineTracker(lineTrackerId, 2, 3, 4, "progressing")

  val oldTracker = LineTracker(lineTrackerId, 0, 8, 9, "progressing")

  val updatedTracker = LineTracker(lineTrackerId, 3, 11, 13, "progressing")

  val service = mock[LineTrackerService]

  when(service.createEmptyTracker) thenReturn lineTracker
  when(service.updateTracker(oldTracker, lineTracker)) thenReturn updatedTracker

}
