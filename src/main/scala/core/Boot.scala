package core

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.util.Timeout
import core.web.MainRouterActor
import spray.can.Http
import scala.concurrent.duration._
import akka.pattern.ask

object Boot extends App {
  implicit val system = ActorSystem("akka-system")
  val service = system.actorOf(Props[MainRouterActor], "main-router-service")

  implicit val timeout = Timeout(5.seconds)
  IO(Http) ? Http.Bind(service, interface = "192.168.1.15", port = 8080)
}
