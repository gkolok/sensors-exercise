package com.epam.sensorstats

import cats.Monoid
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should._

class SensorStatisticsTest extends AnyFunSuite with Matchers {
  test("NanStatistics combime v: ValidSensorStatistics should be v") {
    val s = ValidSensorStatistics(11, 1, 15, 3.5)
    Monoid[SensorStatistics].combine(NanStatistics, s) shouldBe s
    Monoid[SensorStatistics].combine(s, NanStatistics) shouldBe s
  }

  test("NanStatistics combime NanStatistics should be NanStatistics") {
    Monoid[SensorStatistics].combine(NanStatistics, NanStatistics) shouldBe NanStatistics
  }

  test("x: ValidSensorStatistics combine y: ValidSensorStatistics") {
    val x = ValidSensorStatistics(11, 1, 15, 3.5)
    val y = ValidSensorStatistics(21, 5, 25, 5.5)
    Monoid[SensorStatistics].combine(x, y) shouldBe ValidSensorStatistics(x.numberOfMeasurements + y.numberOfMeasurements, x.min, y.max, (11 * 3.5 + 21 * 5.5) / (11 + 21))
  }
}
