ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.2.0"

lazy val root = (project in file("."))
  .settings(
    name := "raft",
    assembly / mainClass := Some ("main"),
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", xs@_*) =>
        (xs map {
          _.toLowerCase
        }) match {
          case "services" :: xs =>
            MergeStrategy.filterDistinctLines
          case _ => MergeStrategy.discard
        }
      case PathList("reference.conf") => MergeStrategy.concat
      case x => MergeStrategy.first
    }
  )
