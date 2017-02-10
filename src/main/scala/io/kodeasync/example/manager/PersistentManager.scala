package io.kodeasync.example.manager

import akka.actor.{ActorLogging, ActorSystem, Props, ReceiveTimeout}
import akka.persistence.{PersistentActor, RecoveryCompleted, SnapshotMetadata, SnapshotOffer}
import akka.util.Timeout
import io.kodeasync.example.models.Common._

import scala.concurrent.duration._
import io.kodeasync.example.models.readtracker.LineTracker
import io.kodeasync.example.service.LineTrackerService

/**
  * Created by shishir on 8/5/16.
  */
object PersistentManager {

  def lineManager(implicit system:ActorSystem) = Props(new PersistentManager(new LineTrackerService))

}

class PersistentManager(var service: LineTrackerService) extends PersistentActor with ActorLogging {

  import akka.cluster.sharding.ShardRegion.Passivate

  protected var persistentResource: LineTracker = null
  protected var snapshotMetaData: SnapshotMetadata = null
  implicit val timeout = Timeout(5 seconds)
  implicit val ec = context.dispatcher
  implicit val actorSystem = context.system
  var trackerId: Option[ID] = None
  context.setReceiveTimeout(10.seconds)

  override def persistenceId = s"${self.path.name}"

  def receiveRecover = {

    case SnapshotOffer(metadata, resourceSnapshot: LineTracker) =>

      log.info(s"Recovering ${resourceSnapshot.getClass.getSimpleName} resource, Id=${resourceSnapshot.id}")
      if (resourceSnapshot != null) {
        this.persistentResource = resourceSnapshot
      }else {
        log.error("received a null resourceSnapshot, bad things to come")
      }
      this.snapshotMetaData = metadata
      log.info(s"recovery complete for ${persistentResource.id}")
    case tracker: LineTracker  =>
      this.persistentResource = tracker
    case RecoveryCompleted =>
      if (persistentResource == null){
        log.error(s"recovery completed but persistentResource is null. My persistenceId is $persistenceId and my trackerId is $trackerId")

      }
      log.info(s"recovery completed")
  }

  val receiveCommand: Receive = {

    case request: Envelope =>
      log.debug(s"Received request=$request from sender=${sender()}")
      trackerId match {
        case None =>
          this.trackerId = Some(request.actorId)
        case Some(shard) =>
          log.debug(s"got shard: $shard")
      }
      self forward request.request

    case CreateNewTracker =>
      val resource = service.createEmptyTracker
      self forward CreateTracker(resource)

    case request: CreateTracker =>
      log.debug(s"Received request=$request from sender = ${sender()}")
      persistentResource = request.tracker
      saveSnapshot(persistentResource)
      sender ! CreateTrackerSuccess(persistentResource)

    case request: UpdateTracker =>
      log.debug(s"Received request=$request from sender=${sender()}")
      this.persistentResource = service.updateTracker(persistentResource, request.tracker)
      sender ! UpdateTrackerSuccess(persistentResource)
      saveSnapshot(this.persistentResource)


    case request: GetTracker =>
      log.debug(s"Received request=$request from sender=${sender()}")
      if ((null == persistentResource) && (trackerId.isEmpty)) {

        log.error("failed to get resource because persistentResource is null and there is no trackerId ")
        sender ! GetTrackerError("LineTrackerService", ErrorMessage(s"persistentResource is null "))

      }else if (null == persistentResource && trackerId.isDefined){
        sender ! GetTrackerError("LineTrackerService", ErrorMessage(s"persistentResource is null."))
      }
      else if (persistentResource.id == request.id) {
        sender() ! GetTrackerSuccess(persistentResource)
      } else {
        sender() ! GetTrackerError("LineTrackerService", ErrorMessage("ids do not match"))
      }

    case request: ReceiveTimeout =>
      context.parent ! Passivate(stopMessage = 'stop)
  }

}
