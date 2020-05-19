package property

import java.time.temporal.ChronoUnit
import java.time.{Duration, LocalDateTime, ZoneOffset}

import domain.model.Period
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

  def convertLDiffToLInst(start: LocalDateTime,
                          ld: List[Int]): List[LocalDateTime] =
    ld.foldLeft[(LocalDateTime, List[LocalDateTime])](start, Nil) {
        case ((i, li), d) =>
          val ni = i.plus(d, ChronoUnit.MINUTES)
          (ni, li :+ ni)
      }
      ._2

  def getInstants(start: LocalDateTime): Gen[List[LocalDateTime]] =
    for {
      n <- Gen.chooseNum(0, 9)
      ld <- Gen.listOfN(n, Gen.chooseNum(10, 60))
    } yield convertLDiffToLInst(start, 0 :: ld)

}
