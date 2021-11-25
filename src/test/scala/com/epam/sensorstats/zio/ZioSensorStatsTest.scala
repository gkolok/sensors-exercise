package com.epam.sensorstats.zio

import zio.test.Assertion.equalTo
import zio.test._
import zio.test.environment.TestConsole

object ZioSensorStatsTest extends DefaultRunnableSpec {

  def replaceCrLfTLf(s: String): String = s.replaceAll("\r\n", "\n")

  def assertProgram(args: List[String], expectedOutput: Vector[String]) =
    for {
      _ <- ZioSensorStats.program(args)
      output <- TestConsole.output
    } yield assert(output.map(replaceCrLfTLf))(equalTo(expectedOutput.map(replaceCrLfTLf)))

  def spec = suite("program effect") {

    testM("Missing program argument handled by error message") {
      assertProgram(List.empty, Vector("Folder of sensor data should be given as program argument\n"))
    } +
      testM("Non-existing folder argument handled by error message") {
        val folderName = "xxxxxfolder"
        assertProgram(List(folderName), Vector(s"Folder: $folderName does not exists\n"))
      } +
      testM("processing testdata folder") {
        val expectedOutput = Vector(
          """
            |Num of processed files: 2
            |Num of processed measurements: 10
            |Num of failed measurements: 4
            |
            |Sensors with highest avg humidity:
            |
            |sensor-id,min,avg,max
            |s1,78,98,88.5
            |s2,78,80,79.0
            |s3,NaN,NaN,NaN
            |s4,NaN,NaN,NaN
            |
            |""".stripMargin
        )
        assertProgram(List("testdata"), expectedOutput)
      }
  }


}
