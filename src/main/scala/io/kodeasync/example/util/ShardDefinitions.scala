package io.kodeasync.example.util

import akka.actor.ActorSystem
import io.kodeasync.example.manager.PersistentManager

/**
  * Created by shishir on 8/5/16.
  */
class ShardDefinitions(implicit val actorSystem: ActorSystem) {
  val lineManager = new ShardableResource(5, "lineManager", PersistentManager.lineManager, actorSystem)
}


object ShardDefinitions {

  private var definitions: Option[ShardDefinitions] = None

  def shards(implicit actorSystem: ActorSystem): ShardDefinitions = {
    definitions match {
      case Some(value) =>
        value
      case None =>
        val localDefinitions = new ShardDefinitions()
        definitions = Some(localDefinitions)
        localDefinitions
    }
  }

  def apply(implicit actorSystem: ActorSystem) = new ShardDefinitions()
}
