import java.time.ZoneOffset
import java.time.temporal.{ChronoUnit, TemporalUnit}

import org.scalacheck.Gen

import domain.model.{Adviser, Availability, CoAdviser, Duration, External, Jury, NonEmptyString, Period, Preference, President, Resource, Role, Supervisor, Teacher, Viva}

import java.time
import java.time.{LocalDateTime, LocalTime}
import java.time.format.DateTimeFormatter

val genNonEmptyString: Gen[NonEmptyString] = for {
  s <- Gen.asciiPrintableStr
  if (!s.isEmpty)
} yield NonEmptyString.create(s).get

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

val genNegativeJavaTimeDuration: Gen[java.time.Duration] = for {
  time <- Gen.chooseNum(1, Long.MaxValue)
} yield java.time.Duration.ZERO.minusNanos(time)

val genGreaterThanZeroJavaTimeDuration: Gen[java.time.Duration] = for {
  time <- Gen.chooseNum(1, Long.MaxValue)
} yield java.time.Duration.ZERO.plusNanos(time)

def genPreferences(numberOfPreferences: Int): Gen[List[Preference]] =
  for {
    preferenceValues <- Gen.listOfN(numberOfPreferences, Gen.chooseNum(1, 5))
  } yield preferenceValues.flatMap(value => Preference.create(value).toOption)

def genAvailabilitySequenceOf(
                               numberOfAvailability: Int,
                               startDateTime: LocalDateTime
                             ): Gen[List[Availability]] =
  for {
    temporalSequence <- temporalSequenceFrom(
      startDateTime,
      ChronoUnit.MINUTES,
      numberOfAvailability * 2
    )
    preferences <- genPreferences(numberOfAvailability)
    periods <- List(
      temporalSequence
        .grouped(2)
        .flatMap(pair => Period.create(pair(0), pair(1)).toOption)
    )
    group <- periods
      .zip(preferences)
      .map(pair => Availability.create(pair._1, pair._2))
      .toList
  } yield group
/*
def genTeachersWith(availabilities: List[Availability], roles: List[Role], percentageFactor: Double) = for {
  chunkSize <- availabilities.size * percentageFactor
  ids <- Gen.listOfN(chunkSize, genNonEmptyString)
  names <- Gen.listOfN(chunkSize, genNonEmptyString)
  teachers <- Gen.listOf(genTeacherWith(availabilities, roles))
} yield teachers*/

def genTeacherWith(availabilities: List[Availability],
                   roles: List[Role]): Gen[Teacher] =
  for {
    id <- genNonEmptyString
    name <- genNonEmptyString
  } yield Teacher.create(id, name, availabilities, roles).get

def genExternalWith(availabilities: List[Availability],
                    roles: List[Role]): Gen[External] =
  for {
    id <- genNonEmptyString
    name <- genNonEmptyString
  } yield External.create(id, name, availabilities, roles).get

def genJuryForViva(startDateTime: LocalDateTime): Gen[Jury] = {
  val availabilities = for {

    presidentAvailabilities <- genAvailabilitySequenceOf(5, startDateTime)
    adviserAvailabilities <- genAvailabilitySequenceOf(5, startDateTime)
    coAdviserAvailabilities <- genAvailabilitySequenceOf(5, startDateTime)
    supervisorAvailabilities <- genAvailabilitySequenceOf(5, startDateTime)
  } yield
    (
      presidentAvailabilities,
      adviserAvailabilities,
      coAdviserAvailabilities,
      supervisorAvailabilities
    )

  for {
    availabilities <- availabilities
    president <- genTeacherWith(availabilities._1, List(President()))
    adviser <- genTeacherWith(availabilities._2, List(Adviser()))
    coAdviser <- genTeacherWith(availabilities._3, List(CoAdviser()))
    supervisor <- genExternalWith(availabilities._4, List(Supervisor()))

  } yield
    Jury.create(president, adviser, List(supervisor), List(coAdviser)).get

}

def genVivaForPeriod(period: Period): Gen[Viva] = {

  val duration =
    Duration.create(java.time.Duration.between(period.start, period.end)).get

  for {
    jury <- genJuryForViva(period.start)
    student <- genNonEmptyString
    title <- genNonEmptyString
  } yield Viva.create(student, title, jury, duration)
}

def temporalSequenceFrom(start: LocalDateTime,
                         temporalUnit: TemporalUnit,
                         numberOfTemporal: Int): Gen[List[LocalDateTime]] =
  for {
    temporalNumbers <- Gen.listOfN(
      numberOfTemporal - 1,
      Gen.chooseNum(10, 60)
    )
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
val vivas = genVivaForPeriod(genPeriod)



val vivas = List[Viva](Viva.create(NonEmptyString.create("student"), NonEmptyString.create("title"), ))