package raft.core

import raft.common.{RaftLogEntry, RaftNodeState, RaftPeerState}

import scala.collection.mutable

trait RaftContext[A,B](nodeId: String) {
  val nodeState: RaftNodeState[A,B] = RaftNodeState[A,B]()
  val peerStates: mutable.HashMap[String, RaftPeerState] = mutable.HashMap[String, RaftPeerState]()
  // TODO: RaftClient
  val peers: mutable.HashMap[String, RaftCore[A,B]] = mutable.HashMap[String, RaftCore[A,B]]()

  def addPeerNode(peer: RaftCore[A,B]): Unit =
    if !(nodeId == peer.nodeId || peers.contains(peer.nodeId)) then
      peerStates.addOne(peer.nodeId, RaftPeerState())
      peers.addOne(peer.nodeId, peer)

  def reinitializedPeerStates(): Unit =
    peerStates.foreach {
      case(peerId,peerState) =>
        peerState.nextIndex = nodeState.logs.size
        peerState.matchIndex = 0
    }

  def isMajority: Boolean =
    // voted for himself
    if peerStates.map(kv => if kv._2.voteGranted then 1 else 0).sum > peers.size/2 then true else false
}
