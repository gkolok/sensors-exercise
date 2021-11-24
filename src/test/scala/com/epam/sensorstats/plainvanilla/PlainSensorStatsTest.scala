package com.epam.sensorstats.plainvanilla

import com.epam.sensorstats._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.io.ByteArrayInputStream
import scala.io.BufferedSource

class PlainSensorStatsTest extends AnyFunSuite with Matchers {

  test("parseCsv") {
    val csvInput = new BufferedSource(new ByteArrayInputStream(
      """sensor-id,humidity
        |s2,80
        |s3,NaN
        |s2,78
        |s1,98""".stripMargin.getBytes))

    PlainSensorStats.parseCsv(csvInput).toList shouldBe List(
      ValidMeasurement(SensorId("s2"), 80),
      FailedMeasurement(SensorId("s3")),
      ValidMeasurement(SensorId("s2"), 78),
      ValidMeasurement(SensorId("s1"), 98),
    )
  }

  test("measurementsToGroupStatistics") {
    val input = List(
      ValidMeasurement(SensorId("s2"), 80),
      FailedMeasurement(SensorId("s3")),
      ValidMeasurement(SensorId("s2"), 78),
      ValidMeasurement(SensorId("s1"), 98),
    )

    PlainSensorStats.measurementsToGroupStatistics(input.iterator) shouldBe
      GroupStatistics(
        numberOfMeasurements = 4,
        numberOfFailedMeasurements = 1,
        statistics = Map(
          SensorId("s1") -> ValidSensorStatistics(1, 98, 98, 98.0),
          SensorId("s2") -> ValidSensorStatistics(2, 78, 80, 79.0),
          SensorId("s3") -> NanStatistics,
        )
      )
  }
}
