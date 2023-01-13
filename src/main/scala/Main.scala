import raft.core.RaftCore

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
        print(nodes.map(node => s"${node.state.showState()} votedFor:${node.nodeState.votedFor.getOrElse("-")}").mkString("\n"))
        Thread.sleep(1)
  }
  t.start()
