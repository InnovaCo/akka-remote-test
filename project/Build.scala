import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "remotetest"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    "com.typesafe.akka" %% "akka-remote" % "2.2-SNAPSHOT",
    "com.codahale.metrics" % "metrics-core" % "3.0.0",
    "org.zeromq" %% "zeromq-scala-binding" % "0.0.7",
    jdbc,
    anorm
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
  )

}
