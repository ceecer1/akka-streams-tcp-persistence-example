package io.kodeasync.example

import java.nio.file.{Paths, StandardOpenOption}

import akka.actor.{ActorSystem, Props}
import akka.stream.scaladsl._
import akka.stream._
import akka.stream.actor.ActorPublisherMessage.Cancel
import akka.stream.actor.{ActorPublisher}
import akka.util.ByteString
import io.kodeasync.example.handler.EventActorPublisher.Message
import io.kodeasync.example.handler._
import io.kodeasync.example.handler.MainActor
import io.kodeasync.example.handler.MainActor.{GetPersistence, Start}
import io.kodeasync.example.util.{LineStats, MergeSort, RegexFilter}
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.io.StdIn

/**
  * Created by shishir on 8/4/16.
  */
object Boot {

  val logger = LoggerFactory.getLogger(Boot.getClass)

  val config = ConfigFactory.load()
  val actorSystemName = config.getString("system")
  import system.dispatcher
  implicit val system = ActorSystem(actorSystemName, config)

  //create empty tracker on persistence side
  val main = system.actorOf(MainActor.props)

  main ! Start
  Thread.sleep(8000)

  val actorRef = system.actorOf(Props[EventActorPublisher])
  val pub = ActorPublisher[Message](actorRef)

  implicit val materializer = ActorMaterializer()

  //tcp host port defns
  val (host, port) = ("localhost", 8888)

  def source: Source[String, _] = Source.fromIterator {
    () => Iterator.continually(StdIn readLine)
  }

  val sink: Sink[String, _] = Sink.actorRef(actorRef, Cancel)

  val switch1 = KillSwitches.shared("switch")

  def main(args: Array[String]): Unit = {

    //Trying to send 'resume' argument to the application to begin from the point where it was stopped
    /*args.headOption foreach {
        case "App resume" => {
          //read last state
          main ! GetPersistence
          Thread.sleep(8000)
        }
        case _            => {
          //else start from beginning
          main ! Start
          Thread.sleep(8000)
        }
      }*/

    val g = RunnableGraph.fromGraph(GraphDSL.create() {
      implicit builder =>
        import GraphDSL.Implicits._

        // Source from file
        val A: Outlet[String] = builder.add(randomLines).out

        //does merge sort in each read lines
        val B: FlowShape[String, String] = builder.add(mergeSort)

        //TCP connection source for pushing "stop" command
        val Z: Outlet[String] = builder.add(Source.fromPublisher(pub).map(s => s.m.filter(_ >= ' '))).out

        //val Z: Outlet[String] = builder.add(Source.single("stop")).out

        //trying to hook the stop switch here from tcp command line input
        val S: FlowShape[String, Any] = builder.add(stopFlow)

        //Merging file stream and tcp stream
        //val M: UniformFanInShape[String, String] = builder.add(Merge[String](2))
        // Flows

        //for diverging to sink and actor
        val C: UniformFanOutShape[String, String] = builder.add(Broadcast[String](2))

        //for sending the Line stats to the persistence
        val P: FlowShape[String, LineStats] = builder.add(toTracker)

        //actor subscriber to send data to persistent actors
        val D: Inlet[LineStats] = builder.add(Sink.actorSubscriber[LineStats](Subscriber.props)).in

        //file writer sink
        val E: Inlet[String] = builder.add(writeSink).in

        //tcp 'stop' command sink
        val I: Inlet[Any] = builder.add(Sink.ignore).in

        A.via(switch1.flow) ~> B ~> C
        Z.via(switch1.flow) ~> S ~> I
        D <~ P <~ C
        E <~ C

        ClosedShape
    })

    //tcp server
    Tcp().bind(host, port).runForeach { conn =>
      val receiveSink =
        conn.flow
          .via(Framing.delimiter(ByteString(System.lineSeparator), maximumFrameLength = 512, allowTruncation = true)).map(_.utf8String)
          .to(sink)
      receiveSink.runWith(Source.maybe)
    }

    g.run()


  }

  //val printSink = Flow[String].map(s => println(s))

  val fpath = Paths.get("src/main/resources/data/lines.txt")
  val randomLines: Source[String, Future[IOResult]] = FileIO.fromPath(fpath).via(Framing.delimiter(ByteString(System.lineSeparator),
    1000, allowTruncation = true).map(bs => bs.utf8String))

  val p = Paths.get("src/main/resources/output/output.txt")
  val fileSink = FileIO.toPath(p, Set(StandardOpenOption.CREATE, StandardOpenOption.APPEND))

  val writeSink = Flow[String].map(s => ByteString(s + "\n")).toMat(fileSink)((_, bytesWritten) => bytesWritten)

  val mergeSort = Flow[String].map(s => MergeSort.doSort(s.toVector).mkString)

  val toTracker = Flow[String].map { s =>
    val length = RegexFilter.regex.findAllIn(s).mkString.length
    LineStats(length, s.length)
  }

  //for immediately shutting down system
  val stopFlow = Flow[String].map { s =>
    if (s == "stop") {
      switch1.shutdown()
    }
  }
}
