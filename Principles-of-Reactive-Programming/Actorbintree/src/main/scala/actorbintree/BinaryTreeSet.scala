/**
 * Copyright (C) 2009-2013 Typesafe Inc. <http://www.typesafe.com>
 */
package actorbintree

import akka.actor._
import scala.collection.immutable.Queue
import akka.event.LoggingReceive

object BinaryTreeSet {

  trait Operation {
    def requester: ActorRef
    def id: Int
    def elem: Int
  }

  trait OperationReply {
    def id: Int
  }

  /**
   * Request with identifier `id` to insert an element `elem` into the tree.
   * The actor at reference `requester` should be notified when this operation
   * is completed.
   */
  case class Insert(requester: ActorRef, id: Int, elem: Int) extends Operation

  /**
   * Request with identifier `id` to check whether an element `elem` is present
   * in the tree. The actor at reference `requester` should be notified when
   * this operation is completed.
   */
  case class Contains(requester: ActorRef, id: Int, elem: Int) extends Operation

  /**
   * Request with identifier `id` to remove the element `elem` from the tree.
   * The actor at reference `requester` should be notified when this operation
   * is completed.
   */
  case class Remove(requester: ActorRef, id: Int, elem: Int) extends Operation

  /** Request to perform garbage collection*/
  case object GC

  /**
   * Holds the answer to the Contains request with identifier `id`.
   * `result` is true if and only if the element is present in the tree.
   */
  case class ContainsResult(id: Int, result: Boolean) extends OperationReply

  /** Message to signal successful completion of an insert or remove operation. */
  case class OperationFinished(id: Int) extends OperationReply

}

class BinaryTreeSet extends Actor with ActorLogging {
  import BinaryTreeSet._
  import BinaryTreeNode._

  def createRoot: ActorRef = context.actorOf(BinaryTreeNode.props(0, initiallyRemoved = true))

  var root = createRoot

  // optional
  var pendingQueue = Queue.empty[Operation]

  // optional
  def receive = LoggingReceive(normal)

  // optional
  /** Accepts `Operation` and `GC` messages. */
  val normal: Receive = {

    case o: Operation =>
      //context.become(acknowledge(o.id))
      root ! o
      
    case r: OperationReply =>
      println("AAAAAAAAAAAAAAAAA "+r)
      context.parent ! r
      
    case x =>
      println("????????????????? "+x)
  }

  /**
   * Wait for ACK before continuing
   */
  /*def acknowledge(id: Int): Receive = {

    case r: OperationReply if (r.id == id) =>
      println("ACK")
      context.become(normal)
      
    case r: OperationReply =>
      println("ACK")
      println("ACK: "+id)
      
    case y: ContainsResult => 
      println("KJKJKJKJKJKJK")
      println("lkjlkjlkjlkjl")
      
    case x => 
      println("XXX "+x)
  }*/

  // optional
  /**
   * Handles messages while garbage collection is performed.
   * `newRoot` is the root of the new binary tree where we want to copy
   * all non-removed elements into.
   */
  def garbageCollecting(newRoot: ActorRef): Receive = ???

}

object BinaryTreeNode {
  trait Position

  case object Left extends Position
  case object Right extends Position

  case class CopyTo(treeNode: ActorRef)
  case object CopyFinished

  def props(elem: Int, initiallyRemoved: Boolean) = Props(classOf[BinaryTreeNode], elem, initiallyRemoved)
}

class BinaryTreeNode(val elem: Int, initiallyRemoved: Boolean) extends Actor {

  node =>

  import BinaryTreeNode._
  import BinaryTreeSet._

  var subtrees = Map[Position, ActorRef]()
  var removed = initiallyRemoved

  // optional
  def receive = normal

  def sendChildOrReply(position: Position, o: Operation, r: => OperationReply) = 
    if(subtrees.contains(position))
      subtrees(position) ! o
    else
     o.requester ! r
      
  // optional
  /** Handles `Operation` messages and `CopyTo` requests. */
  val normal: Receive = LoggingReceive {

    case r: Remove =>
      if(initiallyRemoved) {
        
        if(subtrees.contains(Left)) subtrees(Left) ! r
        else r.requester ! OperationFinished(r.id)
      }
      else if(r.elem == elem) {

        removed= true
        r.requester ! OperationFinished(r.id)
      }
      else if(r.elem < elem) {
       
        sendChildOrReply(Left, r, OperationFinished(r.id))
      }
      else if(r.elem > elem){
        
        sendChildOrReply(Right, r, OperationFinished(r.id))
      }       
      
    case c:Contains =>
      if(initiallyRemoved) {
        
        if(subtrees.contains(Left)) subtrees(Left) ! c
        else c.requester ! ContainsResult(c.id, false)
      }
      else if(c.elem == elem) {

        c.requester ! ContainsResult(c.id, !removed)
      }
      else if(c.elem < elem) {
       
        sendChildOrReply(Left, c, ContainsResult(c.id, false))
      }
      else if(c.elem > elem){
        
        sendChildOrReply(Right, c, ContainsResult(c.id, false))
      } 
    
    case i:Insert => 
      if(initiallyRemoved) {
        subtrees += BinaryTreeNode.Left -> context.actorOf(props(i.elem, false))
        i.requester ! OperationFinished(i.id)
      
      } else if(i.elem == elem) {
        
        removed= false
        i.requester ! OperationFinished(i.id)
      
      } else if(i.elem < elem) {
       
        if(subtrees.contains(Left)) subtrees(Left) ! i
        else {
          subtrees += BinaryTreeNode.Left -> context.actorOf(props(i.elem, false))
          i.requester ! OperationFinished(i.id)
        }
      }  else if(i.elem > elem) {
       
        if(subtrees.contains(Right)) subtrees(Right) ! i
        else {
          subtrees += BinaryTreeNode.Right -> context.actorOf(props(i.elem, false))
          i.requester ! OperationFinished(i.id)
        }
      }  
  }

  // optional
  /**
   * `expected` is the set of ActorRefs whose replies we are waiting for,
   * `insertConfirmed` tracks whether the copy of this node to the new tree has been confirmed.
   */
  def copying(expected: Set[ActorRef], insertConfirmed: Boolean): Receive = ???

}
