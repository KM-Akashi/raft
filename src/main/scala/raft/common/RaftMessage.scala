package raft.common

import scala.collection.mutable

case class RaftLogEntry[A, B](term: Int, index: Int, key: A, value: B)
case class RequestVoteMessage(term: Int, candidateId: String, lastLogIndex: Int, lastLogTerm: Int)
case class RaftHeartbeat(term: Int, leaderId: String)
case class AppendEntriesMessage[A, B](term: Int, leaderId: String, prevLogIndex: Int, prevLogTerm: Int, entries: List[RaftLogEntry[A,B]], leaderCommit: Int)