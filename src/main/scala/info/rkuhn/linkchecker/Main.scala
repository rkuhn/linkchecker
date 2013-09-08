package info.rkuhn.linkchecker

import akka.actor.Actor
import akka.actor.Props

class Main extends Actor {
  
  import Receptionist._

  val receptionist = context.actorOf(Props[Receptionist], "receptionist")
  
  receptionist ! Get("http://www.google.com")
  
  def receive = {
    case Result(set) =>
      println(set mkString "\n")
      context.stop(self)
    case Failed =>
      println("Failed")
      context.stop(self)
  }
  
  override def postStop(): Unit = {
    WebClient.shutdown()
  }
  
}