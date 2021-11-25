package com.epam.sensorstats.zio

import cats.Monoid
import com.epam.sensorstats.Util.{parseCsvLine, resultToString}
import com.epam.sensorstats.{GroupStatistics, Measurement}
import zio._
import zio.blocking.Blocking
import zio.console.{Console, putStrLn}
import zio.stream.{ZStream, ZTransducer}

import java.nio.file.{Files, Paths}

object ZioSensorStats extends App {

  private def csvBytesToMeasurements[R, E](stream: ZStream[R, E, Byte]): ZStream[R, E, Measurement] = stream
    .transduce(ZTransducer.utf8Decode)
    .transduce(ZTransducer.splitLines)
    .drop(1) // we do not interested in header line
    .map(parseCsvLine)

  protected[zio] def program(args: List[String]) =
    ZStream(args)
      .mapM(argumentCheck)
      .flatMap(folder => ZStream.fromJavaStream(Files.walk(Paths.get(folder))))
      .filter(path => path.getFileName.toString.endsWith(".csv"))
      .mapMPar(10) { path =>
        csvBytesToMeasurements(ZStream.fromFile(path))
          .map(GroupStatistics.fromMeasurement)
          .fold(GroupStatistics())(Monoid[GroupStatistics].combine)
          .map((1, _))
      }
      .fold((0, GroupStatistics()))(Monoid[(Int, GroupStatistics)].combine)
      .flatMap(x => putStrLn(resultToString(x)))
      .catchSome {
        case e: String => putStrLn(e)
      }

  protected[zio] def argumentCheck(args: List[String]) =
    for {
      _ <- ZIO.when(args.isEmpty)(ZIO.fail("Folder of sensor data should be given as program argument"))
      _ <- ZIO.when(Files.notExists(Paths.get(args.head)))(ZIO.fail(s"Folder: ${args.head} does not exists"))
    } yield args.head

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    program(args).exitCode


}
