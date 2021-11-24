package com.epam.sensorstats.fs2

import _root_.fs2.{Pure, Stream, text}
import cats.effect.IO
import com.epam.sensorstats.fs2.Fs2SensorStats._
import com.epam.sensorstats._
import _root_.fs2.io.file.{Files, Path}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class Fs2SensorStatsTest extends AnyFunSuite with Matchers {
  implicit val runtime = cats.effect.unsafe.IORuntime.global
  val csvInput: Stream[Pure, Byte] = Stream(
    """sensor-id,humidity
      |s2,80
      |s3,NaN
      |s2,78
      |s1,98""".stripMargin)
    .through(text.utf8.encode)

  test("csvBytesToGroupStatistics") {
    val result = csvInput
      .through(csvBytesToGroupStatistics)
      .compile
      .toList
      .unsafeRunSync

    result shouldBe List(GroupStatistics(4, 1, Map(
      SensorId("s3") -> NanStatistics,
      SensorId("s2") -> ValidSensorStatistics(2, 78, 80, 79.0),
      SensorId("s1") -> ValidSensorStatistics(1, 98, 98, 98.0)
    )))
  }

  ignore("writing really large file") {
    (Stream("sensor-id,humidity") ++
      Stream("s1,50").repeat.take(Math.pow(1024, 3).toLong))
      .intersperse("\n")
      .through(text.utf8.encode)
      .through(Files[IO].writeAll(Path("testdata/largedata.csv")))
      .compile.drain.unsafeRunSync()
  }
}
