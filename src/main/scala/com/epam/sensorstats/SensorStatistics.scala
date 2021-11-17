package com.epam.sensorstats

import cats.Monoid

sealed trait SensorStatistics {
  def print: String
}

case class ValidSensorStatistics(numberOfMeasurements: Long, min: Int, max: Int, average: Double) extends SensorStatistics {
  override def print: String = s"$min,$max,$average"
}

case object NanStatistics extends SensorStatistics {
  override def print: String = "NaN,NaN,NaN"
}

object SensorStatistics {
  implicit def monoidSensorStatistics: Monoid[SensorStatistics] = new Monoid[SensorStatistics] {

    override def empty: SensorStatistics = NanStatistics

    override def combine(x: SensorStatistics, y: SensorStatistics): SensorStatistics = (x, y) match {
      case (NanStatistics, b) => b
      case (a, NanStatistics) => a
      case (a: ValidSensorStatistics, b: ValidSensorStatistics) => ValidSensorStatistics(
        numberOfMeasurements = a.numberOfMeasurements + b.numberOfMeasurements,
        min = Math.min(a.min, b.min),
        max = Math.max(a.max, b.max),
        average = (a.numberOfMeasurements * a.average + b.numberOfMeasurements * b.average) / (a.numberOfMeasurements + b.numberOfMeasurements)
      )
    }
  }

  def fromMeasurement(measurement: Measurement): SensorStatistics = measurement match {
    case ValidMeasurement(_, humidity) => ValidSensorStatistics(1, humidity, humidity, humidity)
    case FailedMeasurement(_) => NanStatistics
  }

}
