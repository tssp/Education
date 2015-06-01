package kvstore

import akka.actor.{ OneForOneStrategy, Props, ActorRef, Actor }
import kvstore.Arbiter._
import scala.collection.immutable.Queue
import akka.actor.SupervisorStrategy.Restart
import scala.annotation.tailrec
import akka.pattern.{ ask, pipe }
import akka.actor.Terminated
import scala.concurrent.duration._
import akka.actor.PoisonPill
import akka.actor.OneForOneStrategy
import akka.actor.SupervisorStrategy
import akka.util.Timeout
import akka.event.LoggingReceive

object Replica {
  sealed trait Operation {
    def key: String
    def id: Long
  }
  case class Insert(key: String, value: String, id: Long) extends Operation
  case class Remove(key: String, id: Long) extends Operation
  case class Get(key: String, id: Long) extends Operation

  sealed trait OperationReply
  case class OperationAck(id: Long) extends OperationReply
  case class OperationFailed(id: Long) extends OperationReply
  case class GetResult(key: String, valueOption: Option[String], id: Long) extends OperationReply

  def props(arbiter: ActorRef, persistenceProps: Props): Props = Props(new Replica(arbiter, persistenceProps))
}

class Replica(val arbiter: ActorRef, persistenceProps: Props) extends Actor {
  import Replica._
  import Replicator._
  import Persistence._
  import context.dispatcher

  var kv = Map.empty[String, String]
  // a map from secondary replicas to replicators
  var secondaries = Map.empty[ActorRef, ActorRef]
  // the current set of replicators
  var replicators = Set.empty[ActorRef]

  // initialize replica
  arbiter ! Join
  
  // used when secondary-role to ensure ordering sequence 
  var expectedSnapshotSequence = 0
  
  def receive = {
    case JoinedPrimary   => context.become(leader)
    case JoinedSecondary => context.become(replica)
  }

  val leader: Receive = {
    case Insert(key, value, id) =>
      kv += key -> value
      sender ! OperationAck(id)
      
    case Remove(key, id) =>
      kv -= key
      sender ! OperationAck(id)
      
    case Get(key, id) =>
      sender ! GetResult(key, kv.get(key), id)
  }

  val replica: Receive = LoggingReceive {
    
    case Snapshot(key, valueOption, seq) if seq > expectedSnapshotSequence =>
      // noop

    case Snapshot(key, valueOption, seq) if seq < expectedSnapshotSequence =>
      sender ! SnapshotAck(key, seq)

    case Snapshot(key, valueOption, seq) =>

      valueOption match {

        case Some(value) =>
          kv += key -> value
        case None =>
          kv -= key
      }

      expectedSnapshotSequence += 1
      
      sender ! SnapshotAck(key, seq)
    
    case Get(key, id) =>
      sender ! GetResult(key, kv.get(key), id)
  }

}
