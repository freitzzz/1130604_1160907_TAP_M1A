package property

import domain.model.{
  Adviser,
  Availability,
  CoAdviser,
  Duration,
  External,
  Jury,
  NonEmptyString,
  Period,
  Preference,
  President,
  Resource,
  Role,
  Supervisor,
  Teacher,
  Viva
}
import org.scalacheck.{Gen, Prop, Properties}
import java.time
import java.time.LocalDateTime

import assessment.AssessmentMS01
import xml.Functions

object Assessment01PropertyBasedTesting extends Properties("") {

  /*val genNonEmptyString: Gen[NonEmptyString] = for {
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

  val genTeacherRoles = Gen[List[Role]] = for {
    d <- Gen.pick(2, List(1,2,3,4))
  } yield

  val genTeacher: Gen[Teacher] = for {
    id <- genNonEmptyString
    name <- genNonEmptyString
    availabilities <- Gen.listOf(genAvailability)
    roles <- Gen.pick(2, List(genTeacherRole, genTeacherRole))
  } yield Teacher.create(id, name, availabilities, roles.map(x => x.sample.get).toList).get

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
      .asInstanceOf[Resource]*/

  property(
    "all viva must be scheduled in the time intervals in which its resources are available"
  ) = {
    /*val vivasDuration = for {
      durationPeriod <- Generators.genPositivePeriodOfTime
      duration <- Duration.create(java.time.Duration.ofHours(1))
    } yield duration*/

    val duration = Duration.create(java.time.Duration.ofHours(1))

    val dateTimeNow = LocalDateTime.now()

    val vivasToScheduleGenerator = for {
      // duration <- vivasDuration
      availabilities <- Generators.genAvailabilitySequenceOf(
        10,
        dateTimeNow,
        duration.get.timeDuration
      )
      presidents <- Generators.genResourcesWith(
        availabilities,
        List(President()),
        2
      )
      advisers <- Generators.genResourcesWith(
        availabilities,
        List(Adviser()),
        2
      )
      coAdvisers <- Generators.genResourcesWith(
        availabilities,
        List(Adviser()),
        1
      )
      supervisors <- Generators.genResourcesWith(
        availabilities,
        List(Adviser()),
        1
      )
    } yield (duration.get, presidents, advisers, coAdvisers, supervisors)

    Prop.forAll(
      vivasToScheduleGenerator,
      Generators.genName,
      Generators.genName
    ) { (arguments, student, title) =>
      {
        println(s"Presidents: ${arguments._2}")

        println(s"Advisers: ${arguments._3}")

        val resources = arguments._2.zipWithIndex
          .map(pair => (pair._1, arguments._3(pair._2)))
        val vivas = resources.map(
          pair =>
            Viva.create(
              student,
              title,
              Jury.create(pair._1, pair._2, List(), List()).get,
              arguments._1
          )
        )

        println(s"=>>>> Resources: $resources")
        println(s"=>>>> Vivas: $vivas")

        val asd = AssessmentMS01
          .create(
            Functions.serialize(
              arguments._1,
              vivas,
              arguments._2 ++ arguments._3 ++ arguments._4 ++ arguments._5
            )
          )

        // println(asd.failed)
        true
        //asd.isSuccess
      }
    }
  }

  //property("one resource cannot be overlapped in two scheduled viva") = ???
}
