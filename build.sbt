name := "feed-eater"

version := "1.0"

scalaVersion := "2.11.7"

resolvers += "spray repo" at "http://repo.spray.io"
resolvers += "Repo2" at "http://repo1.maven.org/maven2"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaV = "2.3.9"
  val sprayV = "1.3.3"
  val json = "3.2.11"
  Seq(
    "io.spray"            %%  "spray-can"     % sprayV,
    "io.spray"            %%  "spray-routing" % sprayV,
    "io.spray"            %%  "spray-client" % sprayV,
    "io.spray"            %%  "spray-testkit" % sprayV  % "test",
    "org.json4s"          %%  "json4s-jackson" % json,
    "org.json4s"          %%  "json4s-ext" % json, 
    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test",
    "org.json4s"          %%  "json4s-native" % "3.3.0",
    "org.specs2"          %%  "specs2" % "2.3.11" % "test",
    "org.scalatest"       %%  "scalatest" % "2.2.4" % "test"
  )
}