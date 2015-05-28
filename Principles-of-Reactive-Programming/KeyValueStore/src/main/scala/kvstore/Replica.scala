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

  // join the cluster
  arbiter ! Join
  
  // used when secondary-role to ensure ordering sequence 
  var expectedSnapshotSequence = 0
  
  // actor that serves a persistence layer
  val persistence= context.actorOf(persistenceProps, "persistence")

  // triggers re-send to persistence
  case object Tick

  
  
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

      context.become(handlePersistence(sender, Persist(key, valueOption, seq), SnapshotAck(key, seq)))
      
    case get:Get => handleGet(sender, get)
  }
  
  def handleGet(requester: ActorRef, get: Get) = requester ! GetResult(get.key, kv.get(get.key), get.id) 

  def handlePersistence(requester: ActorRef, persist:Persist, ack: Any): Receive = LoggingReceive { 
    
    import scala.language.postfixOps

    // register periodic schedule to re-transmit persistence message
    val scheduler= context.system.scheduler.schedule(100 millis, 100 millis, self, Tick)
    
    persistence ! persist
    
    {
      case get:Get => 
        handleGet(sender, get) 
      
      case Tick =>
        persistence ! persist
      
      case Persisted(key, id) if key==persist.key && id == persist.id =>
        scheduler.cancel
        requester ! ack
        
    }
  }
}

