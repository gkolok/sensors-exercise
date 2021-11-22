package com.epam.sensorstats

object Util {

  private val statisticsTupleToString = (sensorId: SensorId, sensorStatistics: SensorStatistics) =>
    s"$sensorId,${sensorStatistics.print}"

  private def statisticsComparator(statisticsTuple1: (SensorId, SensorStatistics), statisticsTuple2: (SensorId, SensorStatistics)) =
    (statisticsTuple1._2, statisticsTuple2._2) match {
      case (NanStatistics, NanStatistics) => statisticsTuple1._1.sensorId < statisticsTuple2._1.sensorId
      case (NanStatistics, _) => false
      case (_, NanStatistics) => true
      case (s1: ValidSensorStatistics, s2: ValidSensorStatistics) => s1.average > s2.average
    }

  def printStatistics(sensorStatistics: Map[SensorId, SensorStatistics]): String = sensorStatistics
    .toList
    .sortWith(statisticsComparator)
    .map(statisticsTupleToString.tupled)
    .mkString("\n")

}
