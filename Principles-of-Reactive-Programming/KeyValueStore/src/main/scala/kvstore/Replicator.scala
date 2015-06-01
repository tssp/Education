package kvstore

import akka.actor.Props
import akka.actor.Actor
import akka.actor.ActorRef
import scala.concurrent.duration._

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
  import scala.language.postfixOps

  object TickResendAll
  
  // map from sequence number to pair of sender and request
  var acks = Map.empty[Long, (ActorRef, Replicate)]

  // register period schedule to retransmit all non-acknowledged mesages
  context.system.scheduler.schedule(200 millis, 200 millis, self, TickResendAll)

  var _seqCounter = 0L
  def nextSeq = {
    val ret = _seqCounter
    _seqCounter += 1
    ret
  }

  // convient method
  def sendReplicate(seq: Long, s: ActorRef, r: Replicate): Unit = replica ! Snapshot(r.key, r.valueOption, seq)
  
  /* Behavior for the Replicator */
  def receive: Receive = {
    
    case r:Replicate =>
      val seq = nextSeq
      acks += seq -> (sender, r)
      sendReplicate(seq, sender, r)
      
    case Replicated(key, id) =>
      acks.get(id).foreach { case(origin, replicate) =>
        
        origin ! SnapshotAck(key, replicate.id)
      }
      
      acks -= id
      
      
    case TickResendAll =>
      acks.toList.sortBy(_._1).foreach { case (seq, (s, r)) => sendReplicate(seq, s, r) }      
  }

}
