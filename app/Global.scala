import akka.actor._
import akka.remote.{DisassociatedEvent, RemotingLifecycleEvent, AssociatedEvent}
import com.codahale.metrics._
import play.api.libs.concurrent.Akka._
import play.api.{Play, Logger, GlobalSettings, Application}
import play.api.Play.current
import concurrent.duration._
import Sender._
import concurrent.ExecutionContext.Implicits.global
import sun.misc.{SignalHandler, Signal}

object Global extends GlobalSettings {
  val metrics = new MetricRegistry
  var reporter: Option[JmxReporter] = None

  override def onStart(app: Application) {
    reporter = Some(JmxReporter.forRegistry(metrics).build())
    reporter.get.start()
    val sender = system.actorOf(Props[Sender], "sender")
    val receiver = system.actorOf(Props[Receiver], "receiver")
//    system.eventStream.subscribe()

    Signal.handle(new Signal("USR2"), new SignalHandler {
      def handle(p1: Signal) {
        Logger.debug("got signal " + p1)
        sender ! Sender.Do
      }
    })
  }

  override def onStop(app: Application) {
    if (reporter.isDefined) reporter.get.stop()
  }
}

object Sender {
  val messageSenderNo = Global.metrics.counter(MetricRegistry.name(classOf[Sender], "message-sended-no"))
  val doneTimer = Global.metrics.timer(MetricRegistry.name(classOf[Sender], "done-timer"))
  case object Do
}

class Sender extends Actor {
  import Sender._

  val rport = Play.configuration.getInt("application.rport").getOrElse(throw new IllegalArgumentException("application.rport not set"))
  val path = s"akka.tcp://application@192.168.0.1:$rport/user/receiver"
  val aSel = system.actorSelection(path)

  def receive = {
    case d @ Do => {
      val ps = Play.configuration.getLong("application.packSize").getOrElse(throw new IllegalArgumentException("application.packSize not set"))
      Logger.debug(s"Starting do ${System.nanoTime}")
      val doneTimerCtx = doneTimer.time()
      aSel ! d

      val m = (for (i <- 0 to 1024) yield "A") mkString ""
      Logger.debug(s"Starting do ${System.nanoTime}")
      for(i <- 1L to ps) {
        aSel ! Receiver.Message(m, System.nanoTime)
        messageSenderNo.inc()
      }
      aSel ! Receiver.Done
      doneTimerCtx.stop()
    }
  }
}

object Receiver {
  val messageMeter = Global.metrics.meter(MetricRegistry.name(classOf[Receiver], "message-receive-meter"))
  val receiveIntHist = Global.metrics.histogram(MetricRegistry.name(classOf[Receiver], "receive-int-hist"))
  case class Message(payload: String, createdNano: Long)
  case object Done
}

class Receiver extends Actor {
  import Receiver._

  var lastTs: Option[Long] = None
  var start: Option[Long] = None
  var reply = Play.configuration.getBoolean("application.reply").getOrElse(throw new IllegalArgumentException("application.reply is not set"))

  def receive = {
    case m @ Message(_, ts) => {
      messageMeter.mark()
      if (!lastTs.isEmpty) receiveIntHist.update(System.nanoTime - lastTs.get)
      lastTs = Some(ts)
      sender ! m
    }
    case Do => {
      lastTs = None
      start = Some(System.nanoTime)
    }
    case Done => {
      val end = System.nanoTime
      Logger.debug(s"Session DONE, start: ${start.get}, end: $end, delta: ${end - start.get} (${(end - start.get) / math.pow(1000, 3)} s)")
      if (reply) sender ! Do
    }
  }
}

class EventListener extends Actor {
  def receive = {
    case e: AssociatedEvent =>
    case d: DisassociatedEvent => system.scheduler.scheduleOnce(5 seconds) {

    }
    case e: RemotingLifecycleEvent => Logger.debug("event " + e.toString)
  }
}
