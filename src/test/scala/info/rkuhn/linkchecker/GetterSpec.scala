package info.rkuhn.linkchecker

import akka.testkit.TestKit
import akka.actor.ActorSystem
import org.scalatest.WordSpecLike
import scala.concurrent.Future
import java.util.concurrent.Executor
import org.scalatest.BeforeAndAfterAll
import akka.testkit.ImplicitSender
import akka.actor.Props
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Terminated

class StepParent(child: Props, fwd: ActorRef) extends Actor {
  context.watch(context.actorOf(child, "child"))
  def receive = {
    case Terminated(_) => context.stop(self)
    case msg           => fwd.tell(msg, sender)
  }
}

object GetterSpec {

  val firstLink = "http://www.rkuhn.info/1"

  val bodies = Map(
    firstLink ->
      """<html>
        |  <head><title>Page 1</title></head>
        |  <body>
        |    <h1>A Link</h1>
        |   <a href="http://rkuhn.info/2">click here</a>
        |  </body>
        |</html>""".stripMargin)

  val links = Map(
    firstLink -> Seq("http://rkuhn.info/2"))

  object FakeWebClient extends WebClient {
    def get(url: String)(implicit exec: Executor): Future[String] =
      bodies get url match {
        case None       => Future.failed(BadStatus(404))
        case Some(body) => Future.successful(body)
      }
  }

  def fakeGetter(url: String, depth: Int): Props =
    Props(new Getter(url, depth) {
      override def client = FakeWebClient
    })

}

class GetterSpec extends TestKit(ActorSystem("GetterSpec"))
  with WordSpecLike with BeforeAndAfterAll with ImplicitSender {

  import GetterSpec._

  override def afterAll(): Unit = {
    system.shutdown()
  }

  "A Getter" must {

    "return the right body" in {
      val getter = system.actorOf(Props(new StepParent(fakeGetter(firstLink, 2), testActor)), "rightBody")
      for (link <- links(firstLink))
        expectMsg(Controller.Check(link, 2))
      watch(getter)
      expectTerminated(getter)
    }

    "properly finish in case of errors" in {
      val getter = system.actorOf(Props(new StepParent(fakeGetter("unknown", 2), testActor)), "wrongLink")
      watch(getter)
      expectTerminated(getter)
    }

  }

}