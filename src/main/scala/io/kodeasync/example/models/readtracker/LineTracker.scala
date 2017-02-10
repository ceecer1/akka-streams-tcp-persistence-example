package io.kodeasync.example.models.readtracker

/**
  * Created by shishir on 8/5/16.
  */
case class LineTracker(id: LineTrackerId,
                       readLineNumber: Long,
                       alphaNumChars: Long,
                       totalCharsInLine: Long,
                       taskName: String) extends Serializable {

}
