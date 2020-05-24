package property

import domain.model.{
  Adviser,
  CoAdviser,
  Duration,
  Jury,
  President,
  Resource,
  Supervisor,
  Viva
}
import org.scalacheck.{Gen, Prop, Properties}
import java.time.LocalDateTime

import assessment.AssessmentMS01
import domainSchedulerImpl.MS01Scheduler
import org.scalacheck.Test.Parameters
import xml.Functions

object Assessment01PropertyBasedTesting
    extends Properties("Assessment01PropertyBasedTesting") {

  override def overrideParameters(p: Parameters): Parameters =
    p.withMinSuccessfulTests(1000)

  val generateVivasScheduleInputsWith24HPeriodsOfTime: Gen[
    (Duration,
     List[Resource],
     List[Resource],
     List[List[Resource]],
     List[List[Resource]])
  ] = {
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

    vivasToScheduleGenerator
  }

  val generateVivasScheduleInputsWith60SPeriodsOfTime: Gen[
    (Duration,
     List[Resource],
     List[Resource],
     List[List[Resource]],
     List[List[Resource]])
  ] = {
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

    vivasToScheduleGenerator
  }

  property(
    "all viva must be scheduled in the time intervals in which its resources are available"
  ) = {

    Prop.forAll(
      generateVivasScheduleInputsWith24HPeriodsOfTime,
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

        val schedule = MS01Scheduler.generateScheduledVivas(vivas.toList)

        schedule.forall(_.isSuccess)
      }
    }
  }

  property(
    "totalPreference of scheduled vivas must always be equal to the sum of the individual vivas"
  ) = {

    Prop.forAll(
      generateVivasScheduleInputsWith24HPeriodsOfTime,
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

    Prop.forAll(
      generateVivasScheduleInputsWith24HPeriodsOfTime,
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

  property(
    "even if vivas take seconds to occur all viva must be scheduled in the time intervals in which its resources are available"
  ) = {

    Prop.forAll(
      generateVivasScheduleInputsWith60SPeriodsOfTime,
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

        val schedule = MS01Scheduler.generateScheduledVivas(vivas.toList)

        schedule.forall(_.isSuccess)
      }
    }
  }

  property("vivas scheduled should be in a First Come First Serve order") = {

    Prop.forAll(
      generateVivasScheduleInputsWith24HPeriodsOfTime,
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
            scheduledVivas(pair._2).viva.title == pair._1.title && scheduledVivas(
              pair._2
            ).viva.student == pair._1.student
        )
      }
    }
  }

  property(
    "after schedule vivas update, all vivas resources should be the same (same id)"
  ) = {

    Prop.forAll(
      generateVivasScheduleInputsWith60SPeriodsOfTime,
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

  property(
    "after schedule vivas update, all vivas resources should have different availabilities, except the first schedule viva"
  ) = {

    Prop.forAll(
      generateVivasScheduleInputsWith24HPeriodsOfTime,
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

        val firstViva = vivas(0)

        val firstScheduleViva = scheduledVivas.headOption.get

        val firstVivaResourcesAvailabilitiesIsEqualToFirstScheduleVivaResourcesAvailabilities = firstViva.jury.asResourcesSet.toList.flatMap(
          resource => resource.availabilities
        ) == firstScheduleViva.viva.jury.asResourcesSet.toList
          .flatMap(resource => resource.availabilities)

        val remainingVivasResourcesAvailabilitiesAreDifferentThanTheScheduleOnes =
          vivas
            .drop(1)
            .zipWithIndex
            .forall(
              pair =>
                pair._1.jury.asResourcesSet.toList
                  .flatMap(resource => resource.availabilities) == scheduledVivas
                  .drop(1)(pair._2)
                  .viva
                  .jury
                  .asResourcesSet
                  .toList
                  .flatMap(resource => resource.availabilities)
            )

        firstVivaResourcesAvailabilitiesIsEqualToFirstScheduleVivaResourcesAvailabilities && remainingVivasResourcesAvailabilitiesAreDifferentThanTheScheduleOnes
      }
    }
  }

  property("all scheduled vivas duration must be equal to vivas duration") = {

    Prop.forAll(
      generateVivasScheduleInputsWith24HPeriodsOfTime,
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

          java.time.Duration
            .between(period.start, period.end)
            .equals(vivasDuration.timeDuration)
        })
      }
    }
  }
}
