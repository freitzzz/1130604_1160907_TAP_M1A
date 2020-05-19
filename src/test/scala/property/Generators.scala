package property

import java.time.{LocalDateTime, ZoneOffset}

import domain.model.Period
import org.scalacheck.Gen

import scala.util.Try

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

}
