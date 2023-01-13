package raft.core

import raft.common.{RaftLogEntry, RaftNodeState, RaftPeerState}

import scala.collection.mutable

trait RaftContext[A,B](nodeId: String) {
  val nodeState: RaftNodeState[A,B] = new RaftNodeState[A,B](nodeId)
  val peerStates: mutable.HashMap[String, RaftPeerState] = mutable.HashMap[String, RaftPeerState]()
  val peers: mutable.HashMap[String, RaftCore[A,B]] = mutable.HashMap[String, RaftCore[A,B]]()

  def addPeerNode(peer: RaftCore[A,B]): Unit =
    if !(nodeId == peer.nodeId || peers.contains(peer.nodeId)) then
      peerStates.addOne(peer.nodeId, RaftPeerState(peer.nodeId))
      peers.addOne(peer.nodeId, peer)

  def isMajority: Boolean =
    // voted for himself
    if peerStates.map(kv => if kv._2.voteGranted then 1 else 0).sum > peers.size/2 then true else false
  def clearVoteGranted(): Unit =
    peerStates.foreach(kv => kv._2.voteGranted = false)
}
