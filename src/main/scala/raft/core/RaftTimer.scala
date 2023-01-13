package raft.core

import scala.concurrent.duration.Deadline
import scala.concurrent.duration._
import scala.util.Random

trait RaftTimer(origin:Int, bound:Int) {
  private val random: Random = new Random()
  private var fetchTime: Deadline = getRandomTimeoutDuration.fromNow

  private def getRandomTimeoutDuration: FiniteDuration = (random.nextInt(bound - origin) + origin).milliseconds
  private def getHeartbeatDuration: FiniteDuration = (origin/10).milliseconds

  def isTimeout: Boolean = fetchTime.isOverdue

  def timeoutNow(): Unit =
    fetchTime = Deadline.now

  def resetTimer(): Unit =
    fetchTime = getRandomTimeoutDuration.fromNow

  def setHeartbeat(): Unit =
    fetchTime = getHeartbeatDuration.fromNow

}
