package io.kodeasync.example.util

import akka.actor.{ActorSystem, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings, ShardRegion}
import io.kodeasync.example.models.Common.Envelope

/**
  * Created by shishir on 8/5/16.
  */
class ShardableResource(numberOfShards: Int, shardType: String, actorProps: Props, implicit val actorSystem: ActorSystem) {

  val idExtractor: ShardRegion.ExtractEntityId = {

    case requestEnvelope: Envelope => {
      val id = requestEnvelope.actorId
      val extractedId = id.oid
      (extractedId, requestEnvelope)
    }
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case requestEnvelope: Envelope => {
      val id = requestEnvelope.actorId
      val shardName = id.shardId
      //just keeping this 5
      val shard = numberOfShards
      (shard.toString)

    }
  }

  val shardStart = ClusterSharding(actorSystem).start(shardType, actorProps, ClusterShardingSettings(actorSystem),
    idExtractor, shardResolver)
}