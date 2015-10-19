package core.validation

import java.net.URL
import java.util.Date

import akka.actor.{Actor, ActorLogging, ActorRefFactory}
import core.marshallers.CustomUnmarshallers
import core.validation.XMLDownloader.DownloadMsg
import spray.client.pipelining._
import spray.http.HttpRequest

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.xml.NodeSeq
import core.web.Cache

object XMLDownloader {
  case class DownloadMsg(httpUrl: URL)
}

class XMLDownloaderActor(cache: Cache) extends Actor with ActorLogging {

  def actorRefFactory: ActorRefFactory = context
  def receive: Receive = {
    case DownloadMsg(httpUrl) =>
      log.info("Received download msg for {}", httpUrl)
      download(httpUrl)
  }

  implicit val dispacher = context.dispatcher

  implicit val NodeSeqUnmarshaller = CustomUnmarshallers.GXMLUnmarshaller

  val nodeSeqPipeline: HttpRequest => Future[NodeSeq] = sendReceive ~> unmarshal[NodeSeq]

  def download(httpUrl: URL) {
    val response: Future[NodeSeq] = nodeSeqPipeline(Get(httpUrl.toString))

    response onComplete {
      case Success(nodeSeq) =>
        log.info("Success")
        cache.add(new ValidationResult(httpUrl, ProcessStatus.Success, "Ok"))
      case Failure(error) =>
        log.error("Failure {}", error)
        cache.add(new ValidationResult(httpUrl, ProcessStatus.Failure, error.toString))
    }
  }

}
