package raft.core

import raft.common.{AppendEntriesMessage, RaftHeartbeat, RaftLogEntry, RaftNodeState, RaftPeerState, RequestVoteMessage}

import scala.collection.mutable
import scala.concurrent.duration.Deadline

class RaftCore[A,B](val nodeId: String)
  extends Thread(s"raft-core-<$nodeId>")
    with RaftUtils
    with RaftContext[A,B](nodeId)
    with RaftTimer(150,300):


  private val follower: RaftState[A,B] = new RaftFollowerState(this)
  private val candidate: RaftState[A,B] = new RaftCandidateState(this)
  private val leader: RaftState[A,B] = new RaftLeaderState(this)
  var state: RaftState[A,B] = follower

  private def poll(): Unit =
    if isTimeout then
      state.timeOut()
    Thread.sleep(1)

  def requestVote(message: RequestVoteMessage): (Int, Boolean) = state.requestVote(message)
  def appendEntries(message: AppendEntriesMessage[A,B]): (Int, Boolean) = state.appendEntries(message)

  def getRequestVoteMessage: RequestVoteMessage =
    RequestVoteMessage(
      nodeState.currentTerm,
      nodeId,
      ???,
      ???
    )

  def getAppendEntriesMessage(forPeerId: String):AppendEntriesMessage[A, B] =
    AppendEntriesMessage(
      nodeState.currentTerm,
      nodeId,
      peerStates(forPeerId).nextIndex - 1,
      nodeState.logs.last.term,
      nodeState.logs.slice(peerStates(forPeerId).nextIndex, nodeState.commitIndex),
      nodeState.commitIndex
    )

  def asFollower(): Unit =
    state = follower

  def asCandidate(): Unit =
    nodeState.currentTerm += 1
    nodeState.votedFor = Some(nodeId)
    state = candidate
    state.election()

  def asLeader(): Unit =
    state = leader
    reinitializedPeerStates()

  def request(key :A, value: B): Unit =
    nodeState.addNewLog(key, value)
    ???

  override def run(): Unit = while true do poll()
