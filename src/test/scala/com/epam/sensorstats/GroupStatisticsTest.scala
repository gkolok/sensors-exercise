package com.epam.sensorstats

import cats.Monoid
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import GroupStatistics.monoidProcessingState

class GroupStatisticsTest extends AnyFunSuite with Matchers {
  test("combine") {
    val processingStateA = GroupStatistics(100, 1, Map(
      SensorId("sensor1") -> ValidSensorStatistics(50, 1, 100, 55.5),
      SensorId("sensor2") -> NanStatistics
    ))
    val processingStateB = GroupStatistics(50, 2, Map(
      SensorId("sensor1") -> ValidSensorStatistics(50, 1, 100, 55.5)
    ))

    Monoid[GroupStatistics].combine(processingStateA, processingStateB) shouldBe GroupStatistics(150, 3, Map(
      SensorId("sensor1") -> ValidSensorStatistics(100, 1, 100, 55.5),
      SensorId("sensor2") -> NanStatistics
    ))
  }
}
