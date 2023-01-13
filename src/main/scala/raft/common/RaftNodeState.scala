package raft.common

class RaftNodeState[A,B](val nodeId: String):
  var currentTerm:Int = 0
  var votedFor:Option[String] = None
  var commitIndex:Int = 0

  var logs: List[RaftLogEntry[A,B]] = Nil

object RaftNodeState:
  def apply[A,B](nodeId: String): RaftNodeState[A,B] = new RaftNodeState[A,B](nodeId)