package com.epam.sensorstats.fs2

import cats.effect.{ExitCode, IO, IOApp}
import com.epam.sensorstats._
import _root_.fs2.io.file.{Files, Path}
import _root_.fs2.{Pipe, Stream, text}

import java.nio.file.Paths
import java.nio.file.{Files => JFiles}

object Fs2SensorStats extends IOApp {

  val parseCsv: Pipe[IO, Byte, Measurement] = stream =>
    stream.through(text.utf8.decode)
      .through(text.lines)
      .drop(1) // we do not interested in header line
      .map(_.split(','))
      .collect {
        case Array(sensorId, "NaN") => FailedMeasurement(SensorId(sensorId))
        case Array(sensorId, value) => ValidMeasurement(SensorId(sensorId), value.toInt)
      }

  val statisticsTupleToString = (sensorId: SensorId, sensorStatistics: SensorStatistics) =>
    s"$sensorId,${sensorStatistics.print}"

  val printResult: ((Int, GroupStatistics)) => IO[Unit] = {
    case (numberOfFiles, GroupStatistics(numberOfMeasurements, numberOfFailedMeasurements, statistics)) =>
      IO {
        println(
          s"""
             |Num of processed files: $numberOfFiles
             |Num of processed measurements: $numberOfMeasurements
             |Num of failed measurements: $numberOfFailedMeasurements
             |
             |Sensors with highest avg humidity:
             |
             |sensor-id,min,avg,max
             |${printStatistics(statistics)}
             |""".stripMargin)
      }
  }

  val csvBytesToGroupStatistics: Pipe[IO, Byte, GroupStatistics] = stream => stream
    .through(parseCsv)
    .foldMap(GroupStatistics.fromMeasurement)

  val countAndMergeGroupStatistics: Pipe[IO, GroupStatistics, (Int, GroupStatistics)] = stream => stream
    .foldMap(groupStatistics => (1, groupStatistics))

  val MAX_OPEN_PARALLEL_STREAMS = 10

  val mainProcessing: Pipe[IO, Path, Unit] = stream => stream
    .map { path =>
      Files[IO].readAll(path)
        .through(csvBytesToGroupStatistics)
    }
    .parJoin(MAX_OPEN_PARALLEL_STREAMS)
    .through(countAndMergeGroupStatistics)
    .evalMap(printResult)

  def printErrorMessage[R](message: String): Either[IO[ExitCode], R] =
    Left(IO {
      println(message)
      ExitCode.Error
    })

  def sensorDataPathsInFolder(folder: String): Either[IO[ExitCode], Stream[IO, Path]] =
    if (JFiles.exists(Paths.get(folder))) {
      Right(Files[IO].list(Path(folder)))
    } else {
      printErrorMessage(s"Folder: $folder does not exists")
    }

  def input(folderOption: Option[String]): Either[IO[ExitCode], Stream[IO, Path]] = folderOption
    .map(sensorDataPathsInFolder)
    .getOrElse(printErrorMessage("Folder of sensor data should be given as program argument"))

  def statisticsComparator(statisticsTuple1: (SensorId, SensorStatistics), statisticsTuple2: (SensorId, SensorStatistics)) =
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

  def run(args: List[String]): IO[ExitCode] =
    input(args.headOption) match {
      case Left(errorPrintout) => errorPrintout
      case Right(sensorDataPathStream) => sensorDataPathStream
        .through(mainProcessing)
        .compile
        .drain.map(_ => ExitCode.Success)
    }
}
