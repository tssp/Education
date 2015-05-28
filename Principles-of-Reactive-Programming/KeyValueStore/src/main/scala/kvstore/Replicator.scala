package kvstore

import akka.actor.Props
import akka.actor.Actor
import akka.actor.ActorRef
import scala.concurrent.duration._
import akka.event.LoggingReceive

object Replicator {
  case class Replicate(key: String, valueOption: Option[String], id: Long)
  case class Replicated(key: String, id: Long)

  case class Snapshot(key: String, valueOption: Option[String], seq: Long)
  case class SnapshotAck(key: String, seq: Long)

  def props(replica: ActorRef): Props = Props(new Replicator(replica))
}

class Replicator(val replica: ActorRef) extends Actor {
  import Replicator._
  import Replica._
  import context.dispatcher
  import scala.concurrent.duration._
  import context.dispatcher
  import scala.language.postfixOps

  object TickResendAll

  // map from sequence number to pair of sender and request
  var acks = Map.empty[Long, (ActorRef, Replicate)]
  // a sequence of not-yet-sent snapshots (you can disregard this if not implementing batching)
  var pending = Vector.empty[Snapshot]

  // register period schedule to retransmit all non-acknowledged mesages
  context.system.scheduler.schedule(200 millis, 200 millis, self, TickResendAll)

  var _seqCounter = 0L
  def nextSeq = {
    val ret = _seqCounter
    _seqCounter += 1
    ret
  }

  def receive: Receive = LoggingReceive {
    case r: Replicate =>
      val seq = nextSeq
      acks += seq -> (sender, r)
      sendSnapshot(seq, sender, r)

    case TickResendAll =>
      acks.toList.sortBy(_._1).foreach { case (seq, (s, r)) => sendSnapshot(seq, s, r) }
  }

  def sendSnapshot(seq: Long, s: ActorRef, r: Replicate): Unit =
    replica ! Snapshot(r.key, r.valueOption, seq)
}
