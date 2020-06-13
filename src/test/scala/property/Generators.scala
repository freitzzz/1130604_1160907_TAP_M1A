package property

import java.time.temporal.{ChronoUnit, TemporalUnit}
import java.time.{LocalDateTime, ZoneOffset}

import domain.model._
import org.scalacheck.Gen

object Generators {

  def genId(prefix: String): Gen[NonEmptyString] =
    for {
      uuid <- Gen.uuid
    } yield NonEmptyString.create(prefix + uuid).get

  val genName: Gen[NonEmptyString] = for {
    name <- Gen.identifier
  } yield NonEmptyString.create(name).get

  val genAtMost24HPeriodOfTime: Gen[(LocalDateTime, LocalDateTime)] = for {
    start <- Gen.choose(
      LocalDateTime.MIN.toEpochSecond(ZoneOffset.UTC),
      LocalDateTime.MAX.toEpochSecond(ZoneOffset.UTC) - 86400
    )
    end <- Gen.choose(
      start + 1,
      LocalDateTime
        .ofEpochSecond(start + 1, 0, ZoneOffset.UTC)
        .plusHours(24)
        .toEpochSecond(ZoneOffset.UTC)
    )
  } yield
    (
      LocalDateTime.ofEpochSecond(start, 0, ZoneOffset.UTC),
      LocalDateTime.ofEpochSecond(end, 0, ZoneOffset.UTC)
    )

  val genAtMost60SPeriodOfTime: Gen[(LocalDateTime, LocalDateTime)] = for {
    start <- Gen.choose(
      LocalDateTime.MIN.toEpochSecond(ZoneOffset.UTC),
      LocalDateTime.MAX.toEpochSecond(ZoneOffset.UTC) - 60
    )
    end <- Gen.choose(
      start + 1,
      LocalDateTime
        .ofEpochSecond(start + 1, 0, ZoneOffset.UTC)
        .plusSeconds(60)
        .toEpochSecond(ZoneOffset.UTC)
    )
  } yield
    (
      LocalDateTime.ofEpochSecond(start, 0, ZoneOffset.UTC),
      LocalDateTime.ofEpochSecond(end, 0, ZoneOffset.UTC)
    )

  def genPreferences(numberOfPreferences: Int): Gen[List[Preference]] =
    for {
      preferenceValues <- Gen.listOfN(numberOfPreferences, Gen.chooseNum(1, 5))
    } yield preferenceValues.flatMap(value => Preference.create(value).toOption)

  def genAvailabilitySequenceOf(
    numberOfAvailability: Int,
    startDateTime: LocalDateTime,
    duration: java.time.Duration
  ): Gen[List[Availability]] =
    for {
      temporalSequence <- temporalSequenceFrom(
        startDateTime,
        ChronoUnit.SECONDS,
        numberOfAvailability * 2,
        duration.getSeconds,
        duration.getSeconds * 3600
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

  def genResourcesWith(availabilities: List[Availability],
                       roles: List[Role],
                       minNumberOfResources: Int = 1): Gen[List[Resource]] = {

    if (roles.contains(Supervisor())) {
      genExternalsWith(availabilities, roles, minNumberOfResources)
    } else {
      genTeachersWith(availabilities, roles, minNumberOfResources)
    }

  }

  def genTeachersWith(availabilities: List[Availability],
                      roles: List[Role],
                      minNumberOfTeachers: Int = 1): Gen[List[Teacher]] =
    for {
      numberOfTeachers <- Gen.chooseNum(
        minNumberOfTeachers,
        minNumberOfTeachers + 10
      )
      teachers <- Gen.listOfN(
        numberOfTeachers,
        genTeacherWith(availabilities, roles)
      )
    } yield teachers

  def genExternalsWith(availabilities: List[Availability],
                       roles: List[Role],
                       minNumberOfExternals: Int = 1): Gen[List[External]] =
    for {
      numberOfExternals <- Gen.chooseNum(
        minNumberOfExternals,
        minNumberOfExternals + 10
      )
      externals <- Gen.listOfN(
        numberOfExternals,
        genExternalWith(availabilities, roles)
      )
    } yield externals

  def genTeacherWith(availabilities: List[Availability],
                     roles: List[Role]): Gen[Teacher] =
    for {
      id <- genId("T")
      name <- genName
    } yield Teacher.create(id, name, availabilities, roles).get

  def genExternalWith(availabilities: List[Availability],
                      roles: List[Role]): Gen[External] = {
    for {
      id <- genId("E")
      name <- genName
    } yield External.create(id, name, availabilities, roles).get
  }

  def temporalSequenceFrom(start: LocalDateTime,
                           temporalUnit: TemporalUnit,
                           numberOfTemporal: Int,
                           minTemporalMeasure: Long,
                           maxTemporalMeasure: Long): Gen[List[LocalDateTime]] =
    for {
      temporalNumbers <- Gen.listOfN(
        numberOfTemporal - 1,
        Gen.chooseNum(minTemporalMeasure, maxTemporalMeasure)
      )
    } yield
      foldTemporalNumbersToTemporalSequence(
        start,
        0 :: temporalNumbers,
        temporalUnit
      )

  def foldTemporalNumbersToTemporalSequence(
    start: LocalDateTime,
    temporalNumbers: List[Long],
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
