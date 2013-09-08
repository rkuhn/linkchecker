package info.rkuhn.linkchecker

import akka.actor.Actor
import akka.actor.Props
import akka.actor.Terminated
import akka.actor.SupervisorStrategy
import akka.actor.ActorLogging

object Controller {
  case class Check(url: String, depth: Int)
}

class Controller(url: String, depth: Int = 2) extends Actor with ActorLogging {
  import Controller._
  
  override val supervisorStrategy = SupervisorStrategy.stoppingStrategy
  
  self ! Check(url, depth)
  
  var cache = Set.empty[String]
  
  def receive = {
    case Check(url, depth) =>
      log.info("{} checking {}", depth, url)
      if (!cache(url) && depth > 0)
        context.watch(context.actorOf(Props(new Getter(url, depth - 1))))
      cache += url
    case Terminated(_) =>
      if (context.children.isEmpty) {
        context.parent ! Receptionist.Result(cache)
        context.stop(self)
      }
  }
  
}