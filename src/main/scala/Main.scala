import raft.common.RaftLogEntry
import raft.core.RaftCore

import scala.util.Random

@main
def main(): Unit =
  val nodes = for i <- 1 to 5 yield new RaftCore[String, Int](i.toString)
  nodes.foreach(node => nodes.foreach(peer => node.addPeerNode(peer)))
  nodes.foreach(node => node.start())
  val t = new Thread() {
    override def run(): Unit =
      print(s"\u001b[s")
      while true do
        print(s"\u001b[u")
        print(nodes.map(node => s"${node.state.showState()}").mkString("\n"))
        Thread.sleep(1)
  }
  t.start()

//  val random: Random = new Random()
//  for i <- 0 to 10 do
//    Thread.sleep(random.between(10,1000))
//    nodes(0).request(s"x${i}",random.between(0,100))