package io.kodeasync.example.models

import io.kodeasync.example.models.readtracker.LineTracker

/**
  * Created by shishir on 8/5/16.
  */
object Common {

  class ID(val oid: String, val shardId: String) extends Serializable {

    def canEqual(other: Any): Boolean = other.isInstanceOf[ID]

    override def equals(other: Any): Boolean = other match {
      case that: ID =>
        (that canEqual this) &&
          shardId == that.shardId &&
          oid == that.oid
      case _ => false
    }

    override def hashCode(): Int = {
      val state = Seq(shardId, oid)
      state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
    }

    override def toString = s"ID($oid, $shardId)"


  }

  trait Request
  final case object CreateNewTracker extends Request
  final case class CreateTracker(tracker: LineTracker) extends Request
  final case class GetTracker(id: ID) extends Request
  final case class UpdateTracker(tracker: LineTracker) extends Request

  final case class Envelope(actorId: ID, request: Request) extends Request

  trait Response
  trait SuccessResponse extends Response
  trait FailureResponse extends Response
  trait ErrorResponse extends Response
  case class ErrorMessage(error: String) extends ErrorResponse

  final case class CreateTrackerSuccess(tracker: LineTracker) extends SuccessResponse
  final case class GetTrackerSuccess(tracker: LineTracker) extends SuccessResponse
  final case class UpdateTrackerSuccess(tracker: LineTracker) extends SuccessResponse
  final case class CreateTrackerError(tracker: String, message: ErrorMessage) extends ErrorResponse
  final case class GetTrackerError(tracker: String, message: ErrorMessage) extends ErrorResponse
  final case class GetTrackerNotFound(tracker:String, message:ErrorMessage) extends ErrorResponse


}
