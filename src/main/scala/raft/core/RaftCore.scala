package raft.core

import raft.common.{AppendEntriesMessage, RaftNodeState, RaftPeerState, RequestVoteMessage}

import scala.collection.mutable
import scala.concurrent.duration.Deadline

class RaftCore[A,B](val nodeId: String)
  extends Thread(s"raft-core-<$nodeId>")
    with RaftUtils
    with RaftContext[A,B](nodeId)
    with RaftTimer(150,300):


  val follower: RaftState[A,B] = new RaftFollowerState(this)
  val candidate: RaftState[A,B] = new RaftCandidateState(this)
  val leader: RaftState[A,B] = new RaftLeaderState(this)
  var state: RaftState[A,B] = follower

  def poll(): Unit =
    if isTimeout then
      state.timeOut()
    Thread.sleep(1)

  def requestVote(message: RequestVoteMessage): (Int, Boolean) = state.requestVote(message)
  def appendEntries(message: AppendEntriesMessage[A,B]): (Int, Boolean) = state.appendEntries(message)

  def getRequestVoteMessage: RequestVoteMessage =
    RequestVoteMessage(nodeState.currentTerm,nodeState.nodeId, nodeState.commitIndex, if nodeState.logs.nonEmpty then nodeState.logs(nodeState.commitIndex.toInt).term else 0)

  def asFollower(): Unit =
    state = follower

  def asCandidate(): Unit =
    nodeState.currentTerm += 1
    nodeState.votedFor = Some(nodeId)
    state = candidate
    //println(s"\n${state.showState()} become Candidate")
    state.election()

  def asLeader(): Unit =
    state = leader

  override def run(): Unit = while true do poll()
