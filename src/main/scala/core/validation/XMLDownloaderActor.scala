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
import core.web.Cache
import javax.xml.parsers.SAXParser
import core.marshallers.CustomUnmarshallers
import javax.xml.transform.stream.StreamSource
import core.ValidationSuccess
import core.ValidationFailed
import core.ParsingError
import core.DownloadFailed

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

  implicit val NodeSeqUnmarshaller = CustomUnmarshallers.unmarshaller

  val nodeSeqPipeline: HttpRequest => Future[StreamSource] = sendReceive ~> unmarshal[StreamSource]

  def download(httpUrl: URL) {
    val response: Future[StreamSource] = nodeSeqPipeline(Get(httpUrl.toString))

    response onComplete {
      case Success(xmlSource) =>
        log.info("Success")
        val validationErrors:Option[Seq[ParsingError]] = GXMLSchemaValidator.validate(xmlSource)
        validationErrors match {
          case Some(errors) =>
            log.error(s"XML validation failed with errors")
            cache.add(new ValidationFailed(httpUrl, errors))
          case None =>
            log.info(s"XML validation successful for $httpUrl")
            cache.add(new ValidationSuccess(httpUrl))
        }
      case Failure(error) =>
        log.error(error, s"Fail to download XML from $httpUrl")
        cache.add(new DownloadFailed(httpUrl, error.getMessage))
    }
  }

}
