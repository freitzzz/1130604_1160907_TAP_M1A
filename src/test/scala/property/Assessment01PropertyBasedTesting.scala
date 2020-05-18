package property

import domain.model.{
  Adviser,
  Availability,
  CoAdviser,
  Duration,
  External,
  NonEmptyString,
  Period,
  Preference,
  President,
  Resource,
  Role,
  Supervisor,
  Teacher
}
import org.scalacheck.{Gen, Properties}
import java.time
import java.time.LocalDateTime

class Assessment01PropertyBasedTesting extends Properties("") {

  val genNonEmptyString: Gen[NonEmptyString] = for {
    s <- Gen.asciiPrintableStr
    if (!s.isEmpty)
  } yield NonEmptyString.create(s).get

  val randomPositiveDurations = List[time.Duration](
    time.Duration.ZERO,
    time.Duration.ofHours(1),
    time.Duration.ofHours(2),
    time.Duration.ofHours(3),
    time.Duration.ofHours(4)
  )

  val randomNegativeDurations = List[time.Duration](
    time.Duration.ofHours(-1),
    time.Duration.ofHours(-2),
    time.Duration.ofHours(-3),
    time.Duration.ofHours(-4)
  )

  val randomLocalDateTimes = List[List[LocalDateTime]](
    List(LocalDateTime.now(), LocalDateTime.now().plusHours(1)),
    List(
      LocalDateTime.of(2020, 12, 1, 10, 0, 0),
      LocalDateTime.of(2020, 12, 1, 11, 0, 0)
    ),
    List(
      LocalDateTime.of(2020, 11, 1, 10, 0, 0),
      LocalDateTime.of(2020, 11, 1, 11, 0, 0)
    ),
    List(
      LocalDateTime.of(2020, 10, 1, 10, 0, 0),
      LocalDateTime.of(2020, 10, 1, 11, 0, 0)
    ),
    List(
      LocalDateTime.of(2020, 9, 1, 10, 0, 0),
      LocalDateTime.of(2020, 9, 1, 11, 0, 0)
    ),
    List(
      LocalDateTime.of(2020, 8, 1, 10, 0, 0),
      LocalDateTime.of(2020, 8, 1, 11, 0, 0)
    ),
    List(
      LocalDateTime.of(2020, 7, 1, 10, 0, 0),
      LocalDateTime.of(2020, 7, 1, 11, 0, 0)
    ),
    List(
      LocalDateTime.of(2020, 6, 1, 10, 0, 0),
      LocalDateTime.of(2020, 6, 1, 11, 0, 0)
    ),
  )

  val genPositiveDuration: Gen[Duration] = for {
    d <- Gen.oneOf(randomPositiveDurations)
  } yield Duration.create(d).get

  val genNegativeDuration: Gen[Duration] = for {
    d <- Gen.oneOf(randomNegativeDurations)
  } yield Duration.create(d).get

  val genPeriod: Gen[Period] = for {
    p <- Gen.oneOf(randomLocalDateTimes)
  } yield Period.create(p.head, p.last).get

  val genPreference: Gen[Preference] = for {
    i <- Gen.chooseNum(1, 5)
  } yield Preference.create(i).get

  val genAvailability: Gen[Availability] = for {
    period <- genPeriod
    preference <- genPreference
  } yield Availability.create(period, preference)

  val genRole: Gen[Role] = for {
    r <- Gen.oneOf(Set(President(), Adviser(), CoAdviser(), Supervisor()))
  } yield r

  val genTeacherRole: Gen[Role] = for {
    r <- Gen.oneOf(Set(President(), Adviser(), CoAdviser()))
  } yield r

  val genExternalRole: Gen[Role] = for {
    r <- Gen.oneOf(Set(Supervisor(), CoAdviser()))
  } yield r

  val genPresidentResource: Gen[Resource] = for {
    id <- genNonEmptyString
    name <- genNonEmptyString
    availabilities <- Gen.listOf(genAvailability)
  } yield Teacher.create(id, name, availabilities, List(President())).get

  val genAdviserResource: Gen[Resource] = for {
    id <- genNonEmptyString
    name <- genNonEmptyString
    availabilities <- Gen.listOf(genAvailability)
  } yield Teacher.create(id, name, availabilities, List(Adviser())).get

  val genSupervisorResource: Gen[Resource] = for {
    id <- genNonEmptyString
    name <- genNonEmptyString
    availabilities <- Gen.listOf(genAvailability)
  } yield Teacher.create(id, name, availabilities, List(Supervisor())).get

  val genCoAdviserResource: Gen[Resource] = for {
    id <- genNonEmptyString
    name <- genNonEmptyString
    availabilities <- Gen.listOf(genAvailability)
  } yield External.create(id, name, availabilities, List(CoAdviser())).get

  val genTeacher: Gen[Teacher] = for {
    id <- genNonEmptyString
    name <- genNonEmptyString
    availabilities <- Gen.listOf(genAvailability)
    roles <- Gen.pick(2, Gen.listOf(genTeacherRole))
  } yield Teacher.create(id, name, availabilities, roles).get

  val genValidExternal: Gen[External] = for {
    id <- genNonEmptyString
    name <- genNonEmptyString
    availabilities <- Gen.listOfN(2, genValidAvailability)
    roles <- Gen.listOfN(2, genExternalRole)
  } yield External.create(id, name, availabilities, roles).get

  val genValidResource: Gen[Resource] = for {
    id <- genNonEmptyString
    name <- genNonEmptyString
    availabilities <- Gen.listOf(genValidAvailability)
    roles <- Gen.listOf(genRole)
  } yield
    Resource
      .validResource(id, name, availabilities, roles)
      .get
      .asInstanceOf[Resource]

  val genInvalidResource: Gen[Resource] = for {
    id <- genNonEmptyString
    name <- genNonEmptyString
    availabilities <- Gen.listOf(genInvalidAvailability)
    roles <- Gen.listOf(genRole)
  } yield
    Resource
      .validResource(id, name, availabilities, roles)
      .get
      .asInstanceOf[Resource]
}
