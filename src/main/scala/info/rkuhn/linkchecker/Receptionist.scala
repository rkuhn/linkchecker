package info.rkuhn.linkchecker

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.Terminated

object Receptionist {
  private case class Job(client: ActorRef, url: String)
  case class Get(url: String)
  case class Result(links: Set[String])
  case object Failed
}

class Receptionist extends Actor {
  import Receptionist._

  def receive = runDirectly

  val runDirectly: Receive = {
    case Get(url)      ⇒ startController(sender, url, Vector.empty)
    case Terminated(_) ⇒ // ignore
  }

  def running(client: ActorRef, controller: ActorRef, queue: Vector[Job]): Receive = {
    case r: Result                ⇒ { client ! r; nextJob(queue) }
    case Terminated(`controller`) ⇒ { client ! Failed; nextJob(queue) }
    case Terminated(_)            ⇒ // ignore
    case Get(url)                 ⇒ enqueueJob(controller, client, queue, Job(sender, url))
  }

  def startController(client: ActorRef, url: String, queue: Vector[Job]): Unit = {
    val controller = context.actorOf(Props(new Controller(url)))
    context.watch(controller)
    context.become(running(client, controller, queue))
  }

  def nextJob(queue: Vector[Job]): Unit =
    if (queue.isEmpty) context.become(runDirectly)
    else {
      val Job(client, url) = queue.head
      startController(client, url, queue.tail)
    }

  def enqueueJob(controller: ActorRef, client: ActorRef, queue: Vector[Job], job: Job): Unit = {
    if (queue.size > 10) sender ! Failed
    else context.become(running(controller, client, queue :+ job))
  }

}