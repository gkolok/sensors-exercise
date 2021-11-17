name := "sensors-exercise"

version := "0.1"
scalaVersion := "2.13.7"
val fs2Version = "3.2.2"

// available for 2.12, 2.13, 3.0
libraryDependencies += "co.fs2" %% "fs2-core" % fs2Version
// optional I/O library
libraryDependencies += "co.fs2" %% "fs2-io" % fs2Version
// optional reactive streams interop
libraryDependencies += "co.fs2" %% "fs2-reactive-streams" % fs2Version
// optional scodec interop
libraryDependencies += "co.fs2" %% "fs2-scodec" % fs2Version
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.10" % "test"