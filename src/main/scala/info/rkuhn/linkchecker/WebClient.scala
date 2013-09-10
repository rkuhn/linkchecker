package info.rkuhn.linkchecker

import scala.concurrent.Future
import com.ning.http.client.AsyncHttpClient
import scala.concurrent.Promise
import java.util.concurrent.Executor

object WebClient {

  private val client = new AsyncHttpClient

  case class BadStatus(status: Int) extends RuntimeException

  def get(url: String)(implicit exec: Executor): Future[String] = {
    val f = client.prepareGet(url).execute();
    val p = Promise[String]()
    f.addListener(new Runnable {
      def run = {
        val response = f.get
        if (response.getStatusCode / 100 < 4)
          p.success(response.getResponseBodyExcerpt(131072))
        else p.failure(BadStatus(response.getStatusCode))
      }
    }, exec)
    p.future
  }

  def shutdown(): Unit = client.close()

}

object WebClientTest extends App {
  import scala.concurrent.ExecutionContext.Implicits.global
  WebClient get "http://www.google.com/1" map println foreach (_ ⇒ WebClient.shutdown())
}