package controllers

import play.api._
import play.api.Play.current
import concurrent.ExecutionContext.Implicits.global
import play.api.mvc._
import play.api.libs.json.{JsObject, Json, JsValue}
import play.api.libs.iteratee.{Concurrent, Enumerator, Iteratee}
import play.api.libs.concurrent.{Akka, Promise}
import play.api.libs.iteratee.Concurrent.Channel
import akka.actor.{ActorSelection, ActorRef, Props, Actor}

import scala.concurrent.Future

object Application extends Controller {
//  val a = Akka.system.actorOf(Props[MyActor], "a")

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

//  def test = WebSocket.using[JsValue] { request =>
//    val channelP = Promise[Channel[JsValue]]()
//    val channelF = channelP.future
//
//    val rport = Play.configuration.getInt("application.rport").get
//    val s: ActorSelection = Akka.system.actorSelection(s"akka.tcp://application@127.0.0.1:$rport/user/a")
//    val it = Iteratee.foreach[JsValue] { v=>
//      Logger.debug(Json.prettyPrint(v))
//      a ! Req(v)
//    } mapDone {v => channelF.map(_.eofAndEnd())}
//
//
//    val en = Concurrent.unicast[JsValue](onStart = (c) => {a ! SetChannel(c)})
//    (it, en)
//  }
//
//  def getRef(sel: ActorSelection): Future[ActorRef] = {
//    import akka.pattern.ask
//    sel
//  }
}


