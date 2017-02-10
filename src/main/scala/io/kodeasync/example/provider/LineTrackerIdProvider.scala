package io.kodeasync.example.provider

import io.kodeasync.example.models.readtracker.LineTrackerId

/**
  * Created by shishir on 8/12/16.
  */
object LineTrackerIdProvider {

  def apply(): LineTrackerId =  {
    LineTrackerId(oid = "lineTrackerObjectId", shardId = "lineTrackerShardId")
  }

}
