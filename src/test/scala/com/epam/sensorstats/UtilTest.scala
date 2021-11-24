package com.epam.sensorstats

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class UtilTest extends AnyFunSuite with Matchers {
  test("parseCsvLine") {
    val csvInput = List(
      "s2,80",
      "s3,NaN",
      "s2,78",
      "s1,98")

    csvInput.map(Util.parseCsvLine) shouldBe List(
      ValidMeasurement(SensorId("s2"), 80),
      FailedMeasurement(SensorId("s3")),
      ValidMeasurement(SensorId("s2"), 78),
      ValidMeasurement(SensorId("s1"), 98),
    )
  }

  test("printStatistics result should be ordered by average, NaN should be last") {
    val sensorsStaticsMap = Map (
      SensorId("s3") -> ValidSensorStatistics(11, 1, 1, 2.5),
      SensorId("s1") -> ValidSensorStatistics(11, 1, 1, 1.5),
      SensorId("s0") -> NanStatistics,
      SensorId("s2") -> ValidSensorStatistics(11, 1, 15, 3.5),
      SensorId("s4") -> NanStatistics,
    )

    Util.printStatistics(sensorsStaticsMap) shouldBe "s2,1,15,3.5\ns3,1,1,2.5\ns1,1,1,1.5\ns0,NaN,NaN,NaN\ns4,NaN,NaN,NaN"
  }
}
