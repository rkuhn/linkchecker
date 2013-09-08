package info.rkuhn.linkchecker

import akka.actor.Actor
import akka.pattern.pipe
import java.util.concurrent.Executor
import akka.actor.ActorLogging

class Getter(url: String, depth: Int) extends Actor {

  import context.dispatcher

  implicit val executor = dispatcher.asInstanceOf[Executor]

  WebClient get url pipeTo self

  def receive = {
    case body: String ⇒
      for (link ← findLinks(body))
        context.parent ! Controller.Check(link, depth)
      context.stop(self)
  }

  val A_TAG = "(?i)<a ([^>]+)>.+?</a>".r
  val HREF_ATTR = """\s*(?i)href\s*=\s*"([^"]*)"|'([^']*)'|([^'">\s]+)""".r

  def findLinks(body: String): Iterator[String] = {
    for {
      anchor ← A_TAG.findAllMatchIn(body)
      HREF_ATTR(dquot, quot, bare) ← anchor.subgroups
    } yield
      if (dquot != null) dquot
      else if (quot != null) quot
      else bare
  }

}