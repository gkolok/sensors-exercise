package com.epam.sensorstats.plainvanilla

import cats.Monoid
import com.epam.sensorstats.Util.{parseCsvLine, printStatistics, resultToString}
import com.epam.sensorstats.{FailedMeasurement, GroupStatistics, Measurement, SensorId, ValidMeasurement}

import java.nio.file.{Files, Path, Paths}
import scala.io.BufferedSource
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try, Using}

object PlainSensorStats {

  def main(args: Array[String]): Unit =
    println(program(args.headOption))

  def program(folderOption: Option[String]): String = {
    input(folderOption) match {
      case Left(errorMessage) => errorMessage
      case Right(sensorDataPathIterator) => mainProcessing(sensorDataPathIterator)
    }
  }

  def sensorDataPathsInFolder(folder: String): Either[String, Iterator[Path]] =
    if (Files.exists(Paths.get(folder))) {
      Right(Files.list(Paths.get(folder)).iterator.asScala)
    } else {
      Left(s"Folder: $folder does not exists")
    }

  def input(folderOption: Option[String]) = folderOption
    .map(sensorDataPathsInFolder)
    .getOrElse(Left("Folder of sensor data should be given as program argument"))

  def mainProcessing(sensorDataPaths: Iterator[Path]): String =
    sensorDataPaths
      .filter(path => path.getFileName.toString.endsWith(".csv"))
      .map { path => csvPathToGroupStatistics(path).map((1, _)) }
      .reduce(Monoid[Try[(Int, GroupStatistics)]].combine)
      .fold(exception => s"Processing failure: ${exception}", resultToString)

  def parseCsv(csvSource: BufferedSource): Iterator[Measurement] =
    csvSource
      .getLines
      .drop(1) // we do not interested in header line
      .map(parseCsvLine)

  def csvPathToGroupStatistics(dataPath: Path): Try[GroupStatistics] =
    Using(io.Source.fromFile(dataPath.toFile)) { bufferedSource =>
      measurementsToGroupStatistics(parseCsv(bufferedSource))
    }


  def measurementsToGroupStatistics(measurements: Iterator[Measurement]): GroupStatistics = measurements
    .map(GroupStatistics.fromMeasurement)
    .reduce(GroupStatistics.combine)
}
