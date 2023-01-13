package raft.core

import raft.common.{AppendEntriesMessage, RaftLogEntry, RequestVoteMessage}
import raft.core.RaftCore

abstract class RaftState[A,B]:
  def requestVote(message: RequestVoteMessage): (Int, Boolean)
  def appendEntries(message: AppendEntriesMessage[A,B]): (Int, Boolean)
  def election(): Unit
  def timeOut(): Unit
  def showState(): String

class RaftFollowerState[A,B](val core: RaftCore[A,B]) extends RaftState[A,B]:
  def showState(): String = f"< ${core.nodeState.nodeId}%3s >[ Follower  ] term:${core.nodeState.currentTerm}%4d"
  def requestVote(message: RequestVoteMessage): (Int, Boolean) =
    if message.term < core.nodeState.currentTerm then
      //println(s"${message.candidateId}[${message.term}] --requestVote--> ${showState()}: reject.")
      (core.nodeState.currentTerm, false)
    else
      //println(s"${message.candidateId}[${message.term}] --requestVote--> ${showState()}: accept.")
      core.nodeState.votedFor = Some(message.candidateId)
      core.nodeState.currentTerm = message.term
      core.resetTimer()
      (core.nodeState.currentTerm, true)


  def appendEntries(message: AppendEntriesMessage[A, B]): (Int, Boolean) =
    if message.term < core.nodeState.currentTerm then
      //println(s"${message.leaderId}[${message.term}] -appendEntries-> ${showState()}]: reject.")
      (core.nodeState.currentTerm, false)
    else
      core.resetTimer()
      (core.nodeState.currentTerm, true)


  def election(): Unit = {}

  def timeOut(): Unit =
    core.asCandidate()

class RaftCandidateState[A,B](val core: RaftCore[A,B]) extends RaftState[A,B]:
  def showState(): String = f"< ${core.nodeState.nodeId}%3s >[ Candidate ] term:${core.nodeState.currentTerm}%4d"
  def requestVote(message: RequestVoteMessage): (Int, Boolean) =
    if message.term < core.nodeState.currentTerm then
      //println(s"${message.candidateId}[${message.term}] --requestVote--> ${showState()}: reject.")
      (core.nodeState.currentTerm, false)
    else
      //println(s"${message.candidateId}[${message.term}] --requestVote--> ${showState()}: accept.")
      core.nodeState.votedFor = Some(message.candidateId)
      core.nodeState.currentTerm = message.term
      core.asFollower()
      core.resetTimer()
      (core.nodeState.currentTerm, true)

  def appendEntries(message: AppendEntriesMessage[A, B]): (Int, Boolean) =
    if message.term < core.nodeState.currentTerm then
      //println(s"${message.leaderId}[${message.term}] -appendEntries-> ${showState()}]: reject.")
      (core.nodeState.currentTerm, false)
    else
      core.asFollower()
      core.resetTimer()
      (core.nodeState.currentTerm, true)

  def election(): Unit =
    val message = core.getRequestVoteMessage
    // TODO: parallelism
    core.peers.foreach {
      case(peerId, peer) =>
        val (term, voteGranted) = peer.requestVote(message)
        core.peerStates(peerId).voteGranted = voteGranted
    }
    if core.isMajority then
      //println(s"\n${showState()} win election, become Leader.")
      core.asLeader()
      core.timeoutNow()

  def timeOut(): Unit =
    core.asCandidate()

class RaftLeaderState[A,B](val core: RaftCore[A,B]) extends RaftState[A,B]:
  def showState(): String = f"< ${core.nodeState.nodeId}%3s >[ Leader    ] term:${core.nodeState.currentTerm}%4d"
  def requestVote(message: RequestVoteMessage): (Int, Boolean) =
    if message.term < core.nodeState.currentTerm then
      //println(s"${message.candidateId}[${message.term}] --requestVote--> ${showState()}: reject.")
      (core.nodeState.currentTerm, false)
    else
      //println(s"${message.candidateId}[${message.term}] --requestVote--> ${showState()}: accept.")
      core.nodeState.votedFor = Some(message.candidateId)
      core.nodeState.currentTerm = message.term
      core.asFollower()
      core.resetTimer()
      (core.nodeState.currentTerm, true)

  def appendEntries(message: AppendEntriesMessage[A, B]): (Int, Boolean) =
    if message.term < core.nodeState.currentTerm then
      //println(s"${message.leaderId}[${message.term}] -appendEntries-> ${showState()}]: reject.")
      (core.nodeState.currentTerm, false)
    else
      core.asFollower()
      core.resetTimer()
      (core.nodeState.currentTerm, true)

  def election(): Unit = {}
  def timeOut(): Unit =
    val message = AppendEntriesMessage(
      core.nodeState.currentTerm,
      core.nodeId,
      0,
      0,
      List[RaftLogEntry[A, B]](),
      core.nodeState.commitIndex
    )
    core.peers.foreach {
      case (peerId, peer) =>
        val (term, success) = peer.appendEntries(message)
    }
    core.setHeartbeat()

