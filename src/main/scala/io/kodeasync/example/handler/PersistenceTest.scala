package io.kodeasync.example.handler

import akka.actor.ActorSystem
import io.kodeasync.example.handler.MainActor.GetPersistence
import io.kodeasync.example.manager.PersistentManagerResolver

/**
  * Created by shishir on 8/5/16.
  * */
object PersistenceTest extends App {

  implicit val system = ActorSystem("akka-programming-test")

  val resolver = system.actorOf(PersistentManagerResolver.props)

  val main = system.actorOf(MainActor.props(system))

  //main ! Start
  //main ! UpdatePersistence
  main ! GetPersistence

}

