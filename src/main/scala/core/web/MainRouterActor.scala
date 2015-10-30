package core.web

import java.net.URL
import akka.actor._
import core.validation.XMLDownloader.DownloadMsg
import core.validation.XMLDownloaderActor
import spray.http.MediaTypes
import spray.routing.HttpService
import org.json4s._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.write
import org.json4s.ext.EnumSerializer
import core.ErrorType
import core.ProcessingState
import core.URLSerializer
import core.ResultSerializer


class MainRouterActor extends Actor with MainRouterService {

  def actorRefFactory: ActorRefFactory = context

  def receive = runRoute(webRoute)
}

trait MainRouterService extends HttpService {

  implicit def executionContext = actorRefFactory.dispatcher
  implicit val formats = ResultSerializer.serializer

  val cache = new Cache()
  val xmlDownloaderActor = actorRefFactory.actorOf(Props(new XMLDownloaderActor(cache)))

  val webRoute =
    path("") {
      get {
        respondWithMediaType(MediaTypes.`text/html`) {
          compressResponse() {
            getFromResource("html/index.html")
          }
        }
      }
    } ~
      path("validation") {
        get {
          respondWithMediaType(MediaTypes.`text/html`) {
            compressResponse() {
              getFromResource("html/validation/validation.html")
            }
          }
        }
      } ~
      path("validation" / "all") {
        get {
          respondWithMediaType(MediaTypes.`application/json`) {
            complete {
              write(cache.listAll())
            }
          }
        }
      } ~
      path("validation" / "add") {
        post {
          formField("url") { url =>
            complete {
              xmlDownloaderActor ! DownloadMsg(new URL(url))
              "Sent to downloader/validator"
            }
          }
        }
      } ~
      path("combiner") {
        get {
          respondWithMediaType(MediaTypes.`text/html`) {
            compressResponse() {
              getFromResource("html/validation/validation.html")
            }
          }
        }
      }
}

class Cache {

  val queue = new scala.collection.mutable.Queue[ProcessingState]
  val maxSize = 10

  def add(validationResult: ProcessingState): Unit = {
    queue += validationResult

    if (queue.length > maxSize)
      queue.dequeue()
  }

  def listAll(): List[ProcessingState] = {
    queue.toList
  }
}