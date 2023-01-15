package raft.common


class RaftNodeState[A,B]():
  var currentTerm:Int = 0
  var votedFor:Option[String] = None
  var commitIndex:Int = -1

  var logs: List[RaftLogEntry[A,B]] = Nil

  def addNewLog(key: A,value: B): RaftLogEntry[A, B] =
    val newLog = RaftLogEntry[A, B](currentTerm, logs.size, key, value)
    logs = logs.appended(newLog)
    newLog

  def lastLog: RaftLogEntry[A, B] =
    if logs.isEmpty then
      RaftLogEntry[A, B](currentTerm, -1, null.asInstanceOf[A], null.asInstanceOf[B])
    else
      logs.last
  def lastCommittedLog:RaftLogEntry[A, B] =
    if logs.isEmpty then
      RaftLogEntry[A,B](currentTerm, commitIndex, null.asInstanceOf[A], null.asInstanceOf[B])
    else if logs.size < commitIndex then
      commitIndex = logs.size - 1
      logs(commitIndex)

object RaftNodeState:
  def apply[A,B](): RaftNodeState[A,B] = new RaftNodeState[A,B]()