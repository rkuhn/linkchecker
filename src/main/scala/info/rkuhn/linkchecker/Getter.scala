package info.rkuhn.linkchecker

import akka.actor.Actor
import akka.pattern.pipe
import java.util.concurrent.Executor
import akka.actor.ActorLogging
import akka.actor.Status
import scala.concurrent.ExecutionContext

object Getter {
  case object Done
  case object Abort
}

class Getter(url: String, depth: Int) extends Actor {
  import Getter._

  implicit val executor = context.dispatcher.asInstanceOf[Executor with ExecutionContext]

  WebClient get url pipeTo self

  def receive = {
    case body: String ⇒
      for (link ← findLinks(body))
        context.parent ! Controller.Check(link, depth)
      stop()
    case _: Status.Failure ⇒ stop()
    case Abort             ⇒ stop()
  }

  def stop(): Unit = {
    context.parent ! Done
    context.stop(self)
  }

  val A_TAG = "(?i)<a ([^>]+)>.+?</a>".r
  val HREF_ATTR = """\s*(?i)href\s*=\s*(?:"([^"]*)"|'([^']*)'|([^'">\s]+))\s*""".r

  def findLinks(body: String): Iterator[String] = {
    for {
      anchor ← A_TAG.findAllMatchIn(body)
      HREF_ATTR(dquot, quot, bare) ← anchor.subgroups
    } yield if (dquot != null) dquot
    else if (quot != null) quot
    else bare
  }

}