package raft.common

import scala.collection.mutable

case class RaftLogEntry[A,B](val term: Int, val key: A, val value: B)
case class RequestVoteMessage(term: Int, candidateId: String, lastLogIndex: Int, lastLogTerm: Int)
case class AppendEntriesMessage[A,B](term: Int, leaderId: String, prevLogIndex: Int, prevLogTerm: Int, entries: List[RaftLogEntry[A,B]], leaderCommit: Int)