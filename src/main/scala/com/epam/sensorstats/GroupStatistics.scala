package com.epam.sensorstats

import cats.Monoid

case class GroupStatistics(numberOfMeasurements: Long = 0, numberOfFailedMeasurements: Long = 0, statistics: Map[SensorId, SensorStatistics] = Map.empty)

object GroupStatistics {
  implicit def monoidProcessingState: Monoid[GroupStatistics] = new Monoid[GroupStatistics] {

    override def empty: GroupStatistics = GroupStatistics()

    override def combine(x: GroupStatistics, y: GroupStatistics): GroupStatistics =
      GroupStatistics(
        numberOfMeasurements = x.numberOfMeasurements + y.numberOfMeasurements,
        numberOfFailedMeasurements = x.numberOfFailedMeasurements + y.numberOfFailedMeasurements,
        statistics = Monoid[Map[SensorId, SensorStatistics]].combine(x.statistics, y.statistics))
  }

  def fromMeasurement(measurement: Measurement): GroupStatistics =
    GroupStatistics(
      numberOfMeasurements = 1,
      numberOfFailedMeasurements = if (measurement.isFailed) 1 else 0,
      statistics = Map(measurement.sensorId -> SensorStatistics.fromMeasurement(measurement))
    )

}
