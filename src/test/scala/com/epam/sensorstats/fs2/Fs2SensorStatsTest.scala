package com.epam.sensorstats.fs2

import _root_.fs2.{Pure, Stream, text}
import com.epam.sensorstats.fs2.Fs2SensorStats._
import com.epam.sensorstats.{FailedMeasurement, GroupStatistics, NanStatistics, SensorId, ValidMeasurement, ValidSensorStatistics}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class Fs2SensorStatsTest extends AnyFunSuite with Matchers {
  implicit val runtime = cats.effect.unsafe.IORuntime.global
  val csvInput: Stream[Pure, Byte] = Stream(
    """sensor-id,humidity
      |s2,80
      |s3,NaN
      |s2,78
      |s1,98
      """.stripMargin)
    .through(text.utf8.encode)

  test("parseCSV") {
    val result = csvInput
      .through(parseCsv)
      .compile
      .toList
      .unsafeRunSync

    result shouldBe List(
      ValidMeasurement(SensorId("s2"), 80),
      FailedMeasurement(SensorId("s3")),
      ValidMeasurement(SensorId("s2"), 78),
      ValidMeasurement(SensorId("s1"), 98),
    )

  }

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

  test("processing really large file") {

  }
}
