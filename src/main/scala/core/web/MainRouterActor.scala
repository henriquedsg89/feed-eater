package core.web

import java.net.URL

import akka.actor._
import core.validation.XMLDownloader.DownloadMsg
import core.validation.{ValidationResult, XMLDownloaderActor}
import spray.http.MediaTypes
import spray.routing.HttpService

class MainRouterActor extends Actor with MainRouterService {

  def actorRefFactory: ActorRefFactory = context

  def receive = runRoute(webRoute)
}

trait MainRouterService extends HttpService {

  implicit def executionContext = actorRefFactory.dispatcher

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
          respondWithMediaType(MediaTypes.`text/html`) {
            complete {
              cache.listAll().mkString("<br/>")
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

  val queue = new scala.collection.mutable.Queue[ValidationResult]
  val maxSize = 10

  def add(validationResult: ValidationResult): Unit = {
    queue += validationResult

    if (queue.length > maxSize)
      queue.dequeue()
  }

  def listAll(): List[ValidationResult] = {
    queue.toList
  }
}