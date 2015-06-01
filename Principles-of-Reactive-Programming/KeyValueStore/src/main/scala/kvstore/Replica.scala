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
import akka.actor.Cancellable

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
  import scala.language.postfixOps

  // local store
  var kv = Map.empty[String, String]

  // a map from secondary replicas to replicators
  var secondaries = Map.empty[ActorRef, ActorRef]

  // initialize replica
  arbiter ! Join

  // actor that serves a persistence layer
  val persistence = context.actorOf(persistenceProps, "persistence")

  // used when secondary-role to ensure ordering sequence 
  var expectedSnapshotSequence = 0

  // TickResendPersist
  object TickResendPersist
  object TickCancelPersist

  def receive = {
    case JoinedPrimary   => context.become(leader)
    case JoinedSecondary => context.become(replica)
  }

  /**
   * Primary Replica Code
   */
  val leader: Receive = {
    case Insert(key, value, id) =>
      kv += key -> value

      context.become(persistPrimary(sender, secondaries.values.toSet, Persist(key, Some(value), id), OperationAck(id), OperationFailed(id)))

    case Remove(key, id) =>
      kv -= key

      context.become(persistPrimary(sender, secondaries.values.toSet, Persist(key, None, id), OperationAck(id), OperationFailed(id)))

    case Get(key, id) =>
      sender ! GetResult(key, kv.get(key), id)

    case Replicas(nodes) =>
      handleReplicas(nodes)
  }

  /**
   * Secondary Replica Code
   */
  val replica: Receive = {

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

      context.become(persistSecondary(sender, Persist(key, valueOption, seq), SnapshotAck(key, seq)))

    case Get(key, id) =>
      sender ! GetResult(key, kv.get(key), id)
  }

  def handleReplicas(nodes: Set[ActorRef]) = {

    // do some filtering and mapping stuff
    val filtered = nodes.filter(_ != self)

    val obsoleteReplicaNodes = secondaries.filter { case (replica, replicator) => !filtered.contains(replica) }
    val allReplicaNodes = filtered.map { replica => replica -> secondaries.getOrElse(replica, context.actorOf(Props(classOf[Replicator], replica))) }.toMap
    val newReplicaNodes = allReplicaNodes.filter { case (replica, _) => !secondaries.contains(replica) }

    // Kill obsolete replica nodes
    obsoleteReplicaNodes.foreach { case (replica, replicator) => replicator ! PoisonPill }

    kv.zipWithIndex.foreach {

      case ((key, value), id) =>

        newReplicaNodes.foreach {
          case (replica, replicator) =>

            replicator ! Replicate(key, Some(value), id)
        }
    }

    // Finally done!
    secondaries = allReplicaNodes
  }

  var messageQueue = Queue[Operation]()

  def persistPrimary(requester: ActorRef, remaining: Set[ActorRef], persisted: Boolean, persist: Persist, schedulers: Iterable[Cancellable], ack: OperationAck, nak: OperationFailed): Receive = LoggingReceive {

    case r: Remove =>
      messageQueue = messageQueue.enqueue(r)

    case i: Insert =>
      messageQueue = messageQueue.enqueue(i)

    case Replicas(nodes) =>
      
      handleReplicas(nodes)
      
      val rr= remaining.filter { r => nodes.contains(r) }
            
      if(rr.isEmpty && persisted) {
        
        requester ! ack
        
        schedulers.foreach(_.cancel())

        context.become(leader)
      }
      else
        context.become(persistPrimary(requester, rr, persisted, persist, schedulers, ack, nak))

    case Get(key, id) =>
      sender ! GetResult(key, kv.get(key), id)

    case Persisted(key, id) if key == persist.key && id == persist.id =>

      if (remaining.isEmpty) {

        requester ! ack

        while (!messageQueue.isEmpty) {

          val (message, queue) = messageQueue.dequeue

          self ! message

          messageQueue = queue
        }
        schedulers.foreach(_.cancel())

        context.become(leader)
        
      } else {
        
        context.become(persistPrimary(requester, remaining, true, persist, schedulers, ack, nak))
      }
        

    case TickCancelPersist =>
      schedulers.foreach(_.cancel)

      if (persisted && remaining.isEmpty)
        requester ! ack
      else
        requester ! nak

      while (!messageQueue.isEmpty) {

        val (message, queue) = messageQueue.dequeue

        self ! message

        messageQueue = queue
      }

      context.become(leader)

    case TickResendPersist =>
      if (!persisted) persistence ! persist
 
      remaining.foreach { _ ! Replicate(persist.key, persist.valueOption, persist.id) }

    case r: Replicated =>
      context.become(persistPrimary(requester, remaining - sender, persisted, persist, schedulers, ack, nak))
  }

  def persistPrimary(requester: ActorRef, remaining: Set[ActorRef], persist: Persist, ack: OperationAck, nak: OperationFailed): Receive = LoggingReceive {

    // register periodic schedule to re-transmit persistence message
    val s1 = context.system.scheduler.scheduleOnce(1000 millis, self, TickCancelPersist)
    val s2 = context.system.scheduler.schedule(100 millis, 100 millis, self, TickResendPersist)

    val schedulers = List(s1, s2)

    persistence ! persist

    remaining.foreach { _ ! Replicate(persist.key, persist.valueOption, persist.id) }

    persistPrimary(requester, remaining, false, persist, schedulers, ack, nak)
  }

  def persistSecondary(requester: ActorRef, persist: Persist, ack: SnapshotAck): Receive = LoggingReceive {

    // register periodic schedule to re-transmit persistence message
    val scheduler = context.system.scheduler.schedule(100 millis, 100 millis, self, TickResendPersist)

    persistence ! persist

    {
      case Get(key, id) =>
        sender ! GetResult(key, kv.get(key), id)

      case Persisted(key, id) if key == persist.key && id == persist.id =>
        scheduler.cancel
        requester ! ack

      case TickResendPersist =>
        persistence ! persist

    }
  }
}

