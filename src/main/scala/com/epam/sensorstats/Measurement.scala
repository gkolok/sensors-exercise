package com.epam.sensorstats

case class SensorId(sensorId: String) extends AnyVal {
  override def toString: String = s"$sensorId"
}

sealed trait Measurement {
  def sensorId: SensorId
  def isFailed: Boolean = false
}

case class ValidMeasurement(override val sensorId: SensorId, humidity: Int) extends Measurement

case class FailedMeasurement(override val sensorId: SensorId) extends Measurement {
  override def isFailed: Boolean = true
}
