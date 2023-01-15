package raft.common

class RaftPeerState():
  var nextIndex: Int = 0
  var matchIndex: Int = 0
  var voteGranted: Boolean = false



object RaftPeerState:
  def apply(): RaftPeerState = new RaftPeerState()
