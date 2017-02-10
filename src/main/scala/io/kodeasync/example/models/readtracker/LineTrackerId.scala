package io.kodeasync.example.models.readtracker

import java.util.UUID

import io.kodeasync.example.models.Common.ID

/**
  * Created by shishir on 8/5/16.
  */
case class LineTrackerId(override val oid: String, override val shardId: String) extends ID(oid, shardId)
