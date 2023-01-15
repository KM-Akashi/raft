package raft.core

import raft.common.{AppendEntriesMessage, RaftHeartbeat, RaftLogEntry, RequestVoteMessage}
import raft.core.RaftCore

abstract class RaftState[A,B](val core: RaftCore[A,B]):
  val stateName: String
  def showState(): String = f"<${core.nodeId}%3s>[ ${stateName}%9s ] term:${core.nodeState.currentTerm}%4d Logs:[${core.nodeState.logs.mkString("/")}]"
  def requestVote(message: RequestVoteMessage): (Int, Boolean) =
    if message.term < core.nodeState.currentTerm then
      (core.nodeState.currentTerm, false)
    else
      core.nodeState.votedFor = Some(message.candidateId)
      core.nodeState.currentTerm = message.term
      core.asFollower()
      core.resetTimer()
      (core.nodeState.currentTerm, true)

  def appendEntries(message: AppendEntriesMessage[A,B]): (Int, Boolean) =
    if message.term < core.nodeState.currentTerm ||
      !(core.nodeState.lastCommittedLog.term == message.prevLogTerm || core.nodeState.lastCommittedLog.index == message.prevLogIndex) then
      (core.nodeState.currentTerm, false)
    else
      (core.nodeState.currentTerm, true)

  def heartbeat(message: RaftHeartbeat): (Int, Boolean) =
    if message.term < core.nodeState.currentTerm then
      (core.nodeState.currentTerm, false)
    else
      core.nodeState.currentTerm = message.term
      core.resetTimer()
      (core.nodeState.currentTerm, true)

  def election(): Unit
  def timeOut(): Unit

class RaftFollowerState[A,B](core: RaftCore[A,B]) extends RaftState[A,B](core):
  val stateName: String = "Follower"

  def election(): Unit = {}

  def timeOut(): Unit =
    core.asCandidate()

class RaftCandidateState[A,B](core: RaftCore[A,B]) extends RaftState[A,B](core):
  val stateName: String = "Candidate"

  def election(): Unit =
    val message = core.getRequestVoteMessage
    // TODO: parallelism
    core.peers.foreach {
      case(peerId, peerClient) =>
        val (term, voteGranted) = peerClient.requestVote(message)
        core.peerStates(peerId).voteGranted = voteGranted
    }
    if core.isMajority then
      core.asLeader()
      core.timeoutNow()

  def timeOut(): Unit =
    core.asCandidate()

class RaftLeaderState[A,B](core: RaftCore[A,B]) extends RaftState[A,B](core):
  val stateName: String = "Leader"

  def election(): Unit = {}

  def timeOut(): Unit =
    core.peers.foreach {
      case (peerId, peerClient) =>
        val message = core.getAppendEntriesMessage(peerId)
        val (term, success) = peerClient.appendEntries(message)
    }
    core.setHeartbeat()

