import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

import domain.model.{Adviser, Duration, Period, President}
import org.scalacheck.Gen

Gen.listOf(President(), Adviser()).sample.get

List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11).grouped(2).toList

0.to(0).toList

val duration5h = Duration.create( java.time.Duration.ZERO.plus(5, ChronoUnit.HOURS)).get

val duration4h = Duration.create( java.time.Duration.ZERO.plus(4, ChronoUnit.HOURS)).get

val dateTimeNow = LocalDateTime.now()

val period1to5 = Period.create(dateTimeNow, dateTimeNow.plus(duration5h.timeDuration)).get

val period2to4 = Period.create(dateTimeNow.plusHours(1), dateTimeNow.plus(duration4h.timeDuration)).get

period1to5.overlaps(period2to4)

period2to4.overlaps(period1to5)