package property

import java.time.temporal.{ChronoUnit, TemporalUnit}
import java.time.{Duration, LocalDateTime, ZoneOffset}

import domain.model.{Availability, Period}
import org.scalacheck.Gen

object Generators {

  val genNegativePeriodOfTime: Gen[(LocalDateTime, LocalDateTime)] = for {
    start <- Gen.choose(
      LocalDateTime.MIN.toEpochSecond(ZoneOffset.UTC),
      LocalDateTime.MAX.toEpochSecond(ZoneOffset.UTC)
    )
    end <- Gen.choose(LocalDateTime.MIN.toEpochSecond(ZoneOffset.UTC), start)
  } yield
    (
      LocalDateTime.ofEpochSecond(start, 0, ZoneOffset.UTC),
      LocalDateTime.ofEpochSecond(end, 0, ZoneOffset.UTC)
    )

  val genEqualPeriodOfTime: Gen[(LocalDateTime, LocalDateTime)] = for {
    start <- Gen.choose(
      LocalDateTime.MIN.toEpochSecond(ZoneOffset.UTC),
      LocalDateTime.MAX.toEpochSecond(ZoneOffset.UTC)
    )
  } yield
    (
      LocalDateTime.ofEpochSecond(start, 0, ZoneOffset.UTC),
      LocalDateTime.ofEpochSecond(start, 0, ZoneOffset.UTC)
    )

  val genPositivePeriodOfTime: Gen[(LocalDateTime, LocalDateTime)] = for {
    start <- Gen.choose(
      LocalDateTime.MIN.toEpochSecond(ZoneOffset.UTC),
      LocalDateTime.MAX.toEpochSecond(ZoneOffset.UTC)
    )
    end <- Gen.choose(start, LocalDateTime.MAX.toEpochSecond(ZoneOffset.UTC))
  } yield
    (
      LocalDateTime.ofEpochSecond(start, 0, ZoneOffset.UTC),
      LocalDateTime.ofEpochSecond(end, 0, ZoneOffset.UTC)
    )

  def genPeriodOfTimeLowerThan(
    localDateTime: LocalDateTime
  ): Gen[(LocalDateTime, LocalDateTime)] = {
    for {
      start <- Gen.choose(
        LocalDateTime.MIN.toEpochSecond(ZoneOffset.UTC),
        localDateTime.toEpochSecond(ZoneOffset.UTC)
      )
      end <- Gen.choose(start, localDateTime.toEpochSecond(ZoneOffset.UTC))
    } yield
      (
        LocalDateTime.ofEpochSecond(start, 0, ZoneOffset.UTC),
        LocalDateTime.ofEpochSecond(end, 0, ZoneOffset.UTC)
      )
  }

  def genPeriodOfTimeHigherThan(
    localDateTime: LocalDateTime
  ): Gen[(LocalDateTime, LocalDateTime)] = {
    for {
      start <- Gen.choose(
        localDateTime.toEpochSecond(ZoneOffset.UTC),
        LocalDateTime.MAX.toEpochSecond(ZoneOffset.UTC)
      )
      end <- Gen.choose(start, LocalDateTime.MAX.toEpochSecond(ZoneOffset.UTC))
    } yield
      (
        LocalDateTime.ofEpochSecond(start, 0, ZoneOffset.UTC),
        LocalDateTime.ofEpochSecond(end, 1, ZoneOffset.UTC)
      )
  }

  def genPeriodOfTimeBetween(
    start: LocalDateTime,
    end: LocalDateTime
  ): Gen[(LocalDateTime, LocalDateTime)] = {
    for {
      periodStart <- Gen.choose(
        start.toEpochSecond(ZoneOffset.UTC),
        end.toEpochSecond(ZoneOffset.UTC),
      )
      periodEnd <- Gen.choose(periodStart, end.toEpochSecond(ZoneOffset.UTC))
    } yield
      (
        LocalDateTime.ofEpochSecond(periodStart, 0, ZoneOffset.UTC),
        LocalDateTime.ofEpochSecond(periodEnd, 0, ZoneOffset.UTC)
      )
  }

  val genPeriod: Gen[Period] = for {
    positivePeriod <- genPositivePeriodOfTime
  } yield Period.create(positivePeriod._1, positivePeriod._2).get

  val genNegativeJavaTimeDuration: Gen[Duration] = for {
    time <- Gen.chooseNum(1, Long.MaxValue)
  } yield java.time.Duration.ZERO.minusNanos(time)

  val genGreaterThanZeroJavaTimeDuration: Gen[Duration] = for {
    time <- Gen.chooseNum(1, Long.MaxValue)
  } yield java.time.Duration.ZERO.plusNanos(time)

  val genAvailabilitySequence: Gen[Availability] = for {
    startPeriod <- genPositivePeriodOfTime
    startTemporalSequenceFromStartPeriod <- temporalSequenceFrom(startPeriod._1)
    endTemporalSequenceFromEndPeriod <- temporalSequenceFrom(startPeriod._2)
  } yield Availability.

  def temporalSequenceFrom(
    start: LocalDateTime,
    temporalUnit: TemporalUnit,
    numberOfTemporals: Int
  ): Gen[List[LocalDateTime]] =
    for {
      temporalNumbers <- Gen.listOfN(n, Gen.chooseNum(10, 60))
    } yield
      foldTemporalNumbersToTemporalSequence(
        start,
        0 :: temporalNumbers,
        temporalUnit
      )

  def foldTemporalNumbersToTemporalSequence(
    start: LocalDateTime,
    temporalNumbers: List[Int],
    temporalUnit: TemporalUnit
  ): List[LocalDateTime] =
    temporalNumbers
      .foldLeft[(LocalDateTime, List[LocalDateTime])](start, Nil) {
        case ((temporal, temporalList), temporalNumber) =>
          val nextTemporal = temporal.plus(temporalNumber, temporalUnit)
          (nextTemporal, temporalList :+ nextTemporal)
      }
      ._2

}
