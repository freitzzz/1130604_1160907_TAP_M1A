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
import domainSchedulerImpl.MS01Scheduler
import org.scalacheck.Test.Parameters
import org.scalatest.prop.Configuration
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

  override def overrideParameters(p: Parameters): Parameters =
    p.withMinSuccessfulTests(1000)

  property(
    "all viva must be scheduled in the time intervals in which its resources are available"
  ) = {
    val vivasDuration = for {
      durationPeriod <- Generators.genAtMost24HPeriodOfTime
      duration <- Duration.create(
        java.time.Duration.between(durationPeriod._1, durationPeriod._2)
      )
    } yield duration

    val dateTimeNow = LocalDateTime.now()

    val vivasToScheduleGenerator = for {
      duration <- vivasDuration
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
      coAdvisersLength <- Gen.chooseNum(
        0,
        Math.min(presidents.length, advisers.length)
      )
      supervisorsLength <- Gen.chooseNum(
        0,
        Math.min(presidents.length, advisers.length)
      )
      coAdvisers <- Gen.listOfN(
        coAdvisersLength,
        Generators.genResourcesWith(availabilities, List(CoAdviser()), 0)
      )
      supervisors <- Gen.listOfN(
        supervisorsLength,
        Generators.genResourcesWith(availabilities, List(Supervisor()), 0)
      )
    } yield (duration.get, presidents, advisers, coAdvisers, supervisors)

    Prop.forAll(
      vivasToScheduleGenerator,
      Generators.genName,
      Generators.genName
    ) { (arguments, student, title) =>
      {

        val vivasDuration = arguments._1

        val presidents = arguments._2

        val advisers = arguments._3

        val coAdvisers = arguments._4

        val supervisors = arguments._5

        val lessResourcesGeneratedLength =
          Math.min(presidents.length, advisers.length)

        val resources = (0 until lessResourcesGeneratedLength).map(
          index =>
            (
              presidents(index),
              advisers(index),
              supervisors.lift(index).getOrElse(List()),
              coAdvisers.lift(index).getOrElse(List())
          )
        )

        val vivas = resources.map(
          pair =>
            Viva.create(
              student,
              title,
              Jury.create(pair._1, pair._2, pair._3, pair._4).get,
              vivasDuration
          )
        )

        val schedule = AssessmentMS01
          .create(
            Functions.serialize(
              vivasDuration,
              vivas.toList,
              presidents ++ advisers ++ coAdvisers.flatten ++ supervisors.flatten
            )
          )

        schedule.isSuccess
      }
    }
  }

  property(
    "totalPreference of scheduled vivas must always be equal to the sum of the individual vivas"
  ) = {
    val vivasDuration = for {
      durationPeriod <- Generators.genAtMost24HPeriodOfTime
      duration <- Duration.create(
        java.time.Duration.between(durationPeriod._1, durationPeriod._2)
      )
    } yield duration

    val dateTimeNow = LocalDateTime.now()

    val vivasToScheduleGenerator = for {
      duration <- vivasDuration
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
      coAdvisersLength <- Gen.chooseNum(
        0,
        Math.min(presidents.length, advisers.length)
      )
      supervisorsLength <- Gen.chooseNum(
        0,
        Math.min(presidents.length, advisers.length)
      )
      coAdvisers <- Gen.listOfN(
        coAdvisersLength,
        Generators.genResourcesWith(availabilities, List(CoAdviser()), 0)
      )
      supervisors <- Gen.listOfN(
        supervisorsLength,
        Generators.genResourcesWith(availabilities, List(Supervisor()), 0)
      )

    } yield (duration.get, presidents, advisers, coAdvisers, supervisors)

    Prop.forAll(
      vivasToScheduleGenerator,
      Generators.genName,
      Generators.genName
    ) { (arguments, student, title) =>
      {
        val vivasDuration = arguments._1

        val presidents = arguments._2

        val advisers = arguments._3

        val coAdvisers = arguments._4

        val supervisors = arguments._5

        val lessResourcesGeneratedLength =
          Math.min(presidents.length, advisers.length)

        val resources = (0 until lessResourcesGeneratedLength).map(
          index =>
            (
              presidents(index),
              advisers(index),
              supervisors.lift(index).getOrElse(List()),
              coAdvisers.lift(index).getOrElse(List())
          )
        )

        val vivas = resources.map(
          pair =>
            Viva.create(
              student,
              title,
              Jury.create(pair._1, pair._2, pair._3, pair._4).get,
              vivasDuration
          )
        )

        val scheduledVivasXml = AssessmentMS01
          .create(
            Functions.serialize(
              vivasDuration,
              vivas.toList,
              presidents ++ advisers ++ coAdvisers.flatten ++ supervisors.flatten
            )
          )

        val collectedPreferences =
          Functions.deserializeTotalPreferenceAndIndividualPreferences(
            scheduledVivasXml.get
          )

        collectedPreferences._1 == collectedPreferences._2.sum

      }
    }
  }

  property("one resource cannot be overlapped in two scheduled viva") = {
    val vivasDuration = for {
      durationPeriod <- Generators.genAtMost24HPeriodOfTime
      duration <- Duration.create(
        java.time.Duration.between(durationPeriod._1, durationPeriod._2)
      )
    } yield duration

    val dateTimeNow = LocalDateTime.now()

    val vivasToScheduleGenerator = for {
      duration <- vivasDuration
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
      coAdvisersLength <- Gen.chooseNum(
        0,
        Math.min(presidents.length, advisers.length)
      )
      supervisorsLength <- Gen.chooseNum(
        0,
        Math.min(presidents.length, advisers.length)
      )
      coAdvisers <- Gen.listOfN(
        coAdvisersLength,
        Generators.genResourcesWith(availabilities, List(CoAdviser()), 0)
      )
      supervisors <- Gen.listOfN(
        supervisorsLength,
        Generators.genResourcesWith(availabilities, List(Supervisor()), 0)
      )

    } yield (duration.get, presidents, advisers, coAdvisers, supervisors)

    Prop.forAll(
      vivasToScheduleGenerator,
      Generators.genName,
      Generators.genName
    ) { (arguments, student, title) =>
      {
        val vivasDuration = arguments._1

        val presidents = arguments._2

        val advisers = arguments._3

        val coAdvisers = arguments._4

        val supervisors = arguments._5

        val lessResourcesGeneratedLength =
          Math.min(presidents.length, advisers.length)

        val resources = (0 until lessResourcesGeneratedLength).map(
          index =>
            (
              presidents(index),
              advisers(index),
              supervisors.lift(index).getOrElse(List()),
              coAdvisers.lift(index).getOrElse(List())
          )
        )

        val vivas = resources.map(
          pair =>
            Viva.create(
              student,
              title,
              Jury.create(pair._1, pair._2, pair._3, pair._4).get,
              vivasDuration
          )
        )

        val scheduledVivasTry =
          MS01Scheduler.generateScheduledVivas(vivas.toList)

        val scheduledVivas = scheduledVivasTry.map(x => x.get)

        scheduledVivas.forall(scheduledViva => {
          val period = scheduledViva.period

          val juryMembers = scheduledViva.viva.jury.asResourcesSet

          val otherVivasWithAtLeastOneJuryMemberFromCurrentViva =
            scheduledVivas.filter(
              x =>
                x.viva.jury.asResourcesSet
                  .map(y => y.id)
                  .exists(juryMembers.map(j => j.id))
                  && x.viva.title != scheduledViva.viva.title
            )

          val otherVivasPeriods =
            otherVivasWithAtLeastOneJuryMemberFromCurrentViva.map(x => x.period)

          otherVivasPeriods.forall(p => !p.overlaps(period))
        })
      }
    }
  }

  //property("one resource cannot be overlapped in two scheduled viva") = ???

  property(
    "even if vivas take seconds to occur all viva must be scheduled in the time intervals in which its resources are available"
  ) = {
    val vivasDuration = for {
      durationPeriod <- Generators.genAtMost60SPeriodOfTime
      duration <- Duration.create(
        java.time.Duration.between(durationPeriod._1, durationPeriod._2)
      )
    } yield duration

    val dateTimeNow = LocalDateTime.now()

    val vivasToScheduleGenerator = for {
      duration <- vivasDuration
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
      coAdvisersLength <- Gen.chooseNum(
        0,
        Math.min(presidents.length, advisers.length)
      )
      supervisorsLength <- Gen.chooseNum(
        0,
        Math.min(presidents.length, advisers.length)
      )
      coAdvisers <- Gen.listOfN(
        coAdvisersLength,
        Generators.genResourcesWith(availabilities, List(CoAdviser()), 0)
      )
      supervisors <- Gen.listOfN(
        supervisorsLength,
        Generators.genResourcesWith(availabilities, List(Supervisor()), 0)
      )
    } yield (duration.get, presidents, advisers, coAdvisers, supervisors)

    Prop.forAll(
      vivasToScheduleGenerator,
      Generators.genName,
      Generators.genName
    ) { (arguments, student, title) =>
      {

        val vivasDuration = arguments._1

        val presidents = arguments._2

        val advisers = arguments._3

        val coAdvisers = arguments._4

        val supervisors = arguments._5

        val lessResourcesGeneratedLength =
          Math.min(presidents.length, advisers.length)

        val resources = (0 until lessResourcesGeneratedLength).map(
          index =>
            (
              presidents(index),
              advisers(index),
              supervisors.lift(index).getOrElse(List()),
              coAdvisers.lift(index).getOrElse(List())
          )
        )

        val vivas = resources.map(
          pair =>
            Viva.create(
              student,
              title,
              Jury.create(pair._1, pair._2, pair._3, pair._4).get,
              vivasDuration
          )
        )

        val schedule = AssessmentMS01
          .create(
            Functions.serialize(
              vivasDuration,
              vivas.toList,
              presidents ++ advisers ++ coAdvisers.flatten ++ supervisors.flatten
            )
          )

        schedule.isSuccess
      }
    }
  }

  property(
    "after schedule vivas update, all vivas resources should be the same (same id)"
  ) = {
    val vivasDuration = for {
      durationPeriod <- Generators.genAtMost60SPeriodOfTime
      duration <- Duration.create(
        java.time.Duration.between(durationPeriod._1, durationPeriod._2)
      )
    } yield duration

    val dateTimeNow = LocalDateTime.now()

    val vivasToScheduleGenerator = for {
      duration <- vivasDuration
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
      coAdvisersLength <- Gen.chooseNum(
        0,
        Math.min(presidents.length, advisers.length)
      )
      supervisorsLength <- Gen.chooseNum(
        0,
        Math.min(presidents.length, advisers.length)
      )
      coAdvisers <- Gen.listOfN(
        coAdvisersLength,
        Generators.genResourcesWith(availabilities, List(CoAdviser()), 0)
      )
      supervisors <- Gen.listOfN(
        supervisorsLength,
        Generators.genResourcesWith(availabilities, List(Supervisor()), 0)
      )
    } yield (duration.get, presidents, advisers, coAdvisers, supervisors)

    Prop.forAll(
      vivasToScheduleGenerator,
      Generators.genName,
      Generators.genName
    ) { (arguments, student, title) =>
      {

        val vivasDuration = arguments._1

        val presidents = arguments._2

        val advisers = arguments._3

        val coAdvisers = arguments._4

        val supervisors = arguments._5

        val lessResourcesGeneratedLength =
          Math.min(presidents.length, advisers.length)

        val resources = (0 until lessResourcesGeneratedLength).map(
          index =>
            (
              presidents(index),
              advisers(index),
              supervisors.lift(index).getOrElse(List()),
              coAdvisers.lift(index).getOrElse(List())
          )
        )

        val vivas = resources.map(
          pair =>
            Viva.create(
              student,
              title,
              Jury.create(pair._1, pair._2, pair._3, pair._4).get,
              vivasDuration
          )
        )

        val scheduledVivas = MS01Scheduler
          .generateScheduledVivas(vivas.toList)
          .flatMap(scheduleViva => scheduleViva.toOption)

        vivas.zipWithIndex.forall(
          pair =>
            scheduledVivas(pair._2).viva.jury.asResourcesSet == pair._1.jury.asResourcesSet
        )
      }
    }
  }
}
