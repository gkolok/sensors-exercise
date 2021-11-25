name := "sensors-exercise"

version := "0.1"
scalaVersion := "2.13.7"
val fs2Version = "3.2.2"
val zioVersion = "1.0.12"

// fs2
libraryDependencies += "co.fs2" %% "fs2-core" % fs2Version
libraryDependencies += "co.fs2" %% "fs2-io" % fs2Version
libraryDependencies += "co.fs2" %% "fs2-reactive-streams" % fs2Version
libraryDependencies += "co.fs2" %% "fs2-scodec" % fs2Version

//zio
libraryDependencies += "dev.zio" %% "zio" % zioVersion
libraryDependencies += "dev.zio" %% "zio-streams" % zioVersion

// scalatest
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.10" % "test"

libraryDependencies ++= Seq(
  "dev.zio" %% "zio-test" % zioVersion % "test",
  "dev.zio" %% "zio-test-sbt" % zioVersion % "test"
)
testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
