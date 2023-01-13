package raft.common

class RaftPeerState(val peerId:String):
  var nextIndex: Int = 1
  var matchIndex: Int = 0
  var voteGranted: Boolean = false



object RaftPeerState:
  def apply(peerId:String): RaftPeerState = new RaftPeerState(peerId)
