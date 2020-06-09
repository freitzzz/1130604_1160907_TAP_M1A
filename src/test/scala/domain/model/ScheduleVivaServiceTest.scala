package domain.model

import java.time.LocalDateTime

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class ScheduleVivaServiceTest extends AnyFunSuite with Matchers {

  test(
    "the total preference value of a set of resources should be equal to the sum of each resources's individual availabilities preferences"
  ) {
    val presidentAvailabilityStartDateTime = LocalDateTime.now()

    val presidentAvailabilityEndDateTime =
      presidentAvailabilityStartDateTime.plusMinutes(10)

    val presidentAvailabilityPeriod = Period
      .create(
        presidentAvailabilityStartDateTime,
        presidentAvailabilityEndDateTime
      )
      .get

    val presidentAvailabilityPreference = Preference.create(5).get

    val presidentAvailability = Availability
      .create(presidentAvailabilityPeriod, presidentAvailabilityPreference)

    val adviserAvailabilityStartDateTime =
      presidentAvailabilityStartDateTime.plusMinutes(5)

    val adviserAvailabilityEndDateTime =
      presidentAvailabilityStartDateTime.plusMinutes(15)

    val adviserAvailabilityPeriod = Period
      .create(adviserAvailabilityStartDateTime, adviserAvailabilityEndDateTime)
      .get

    val adviserAvailabilityPreference = Preference.create(3).get

    val adviserAvailability = Availability
      .create(adviserAvailabilityPeriod, adviserAvailabilityPreference)

    val president = Teacher
      .create(
        NonEmptyString.create("1").get,
        NonEmptyString.create("John").get,
        List(presidentAvailability),
        List(President())
      )
      .get

    val adviser =
      Teacher
        .create(
          NonEmptyString.create("2").get,
          NonEmptyString.create("Doe").get,
          List(adviserAvailability),
          List(Adviser())
        )
        .get

    val resourcesSet = Set[Resource](president, adviser)

    val startTime =
      presidentAvailabilityStartDateTime.plusMinutes(5)

    val endTime =
      startTime.plusMinutes(5)

    val period =
      Period.create(startTime, endTime).get

    //Act
    val calculatedPreference =
      ScheduledVivaService.calculateSumOfPreferences(resourcesSet, period)

    val expectedPreference = resourcesSet
      .flatMap(resource => resource.availabilityOn(period))
      .foldLeft(0)(_ + _.preference.value)

    //Assert
    calculatedPreference shouldBe expectedPreference

  }

  test(
    "if a viva has two resources with two availabilities each of 1 hour and the viva duration is 1 hour, then findPeriodsInWhichVivaCanOccur should return a list with those two availability periods"
  ) {

    // Arrange

    val dateTimeNow = LocalDateTime.now()

    val periodX = Period.create(dateTimeNow, dateTimeNow.plusHours(1)).get

    val periodY =
      Period.create(dateTimeNow.plusHours(1), dateTimeNow.plusHours(2)).get

    val preferenceX = Preference.create(5).get

    val preferenceY = Preference.create(5).get

    val availabilityX = Availability.create(periodX, preferenceX)

    val availabilityY = Availability.create(periodY, preferenceY)

    val president = Teacher
      .create(
        NonEmptyString.create("T001").get,
        NonEmptyString.create("President").get,
        List(availabilityX, availabilityY),
        List(President())
      )
      .get

    val adviser = Teacher
      .create(
        NonEmptyString.create("E001").get,
        NonEmptyString.create("Adviser").get,
        List(availabilityX, availabilityY),
        List(Adviser())
      )
      .get

    val jury = Jury.create(president, adviser, List(), List()).get

    val vivaDuration = Duration.create(java.time.Duration.ofHours(1)).get

    val viva = Viva.create(
      NonEmptyString.create("Student").get,
      NonEmptyString.create("Functional Programming is cool").get,
      jury,
      vivaDuration
    )

    // Act

    val findPeriodsInWhichVivaCanOccur =
      ScheduledVivaService.findPeriodsInWhichVivaCanOccur(viva)

    // Assert

    findPeriodsInWhichVivaCanOccur shouldBe List(
      availabilityX.period,
      availabilityY.period
    )
  }

  /*
    Viva 0 - T3, T1, E2 - The viva that was scheduled and these are the resources that we are going to update in the other vivas.
    Viva 1 - T1, T2, E1
    Viva 2 - T2, T3, E2
    Viva 3 - T3, T4, E1
    All these vivas contain at least one resource that needs to be updated.
    Therefore, all vivas must be updated because at least one of their resources
    will have different availabilities after scheduling a viva.
   */
  test(
    "given a list of vivas that share a set of resources between them, then when one viva is scheduled, all resources in the vivas must have their availabilities updated"
  ) {
    val availability = Availability.create(
      Period
        .create(
          LocalDateTime.of(2020, 5, 30, 9, 0, 0),
          LocalDateTime.of(2020, 5, 30, 9, 0, 0).plusHours(5)
        )
        .get,
      Preference.create(5).get
    )

    val t1 = Teacher.create(
      NonEmptyString.create("T1").get,
      NonEmptyString.create("Teacher T1").get,
      List[Availability](availability),
      List[Role](President(), Adviser())
    )

    val t2 = Teacher.create(
      NonEmptyString.create("T2").get,
      NonEmptyString.create("Teacher T2").get,
      List[Availability](availability),
      List[Role](President(), Adviser())
    )

    val t3 = Teacher.create(
      NonEmptyString.create("T3").get,
      NonEmptyString.create("Teacher T3").get,
      List[Availability](availability),
      List[Role](President(), Adviser())
    )

    val t4 = Teacher.create(
      NonEmptyString.create("T4").get,
      NonEmptyString.create("Teacher T4").get,
      List[Availability](availability),
      List[Role](President(), Adviser())
    )

    val e1 = External.create(
      NonEmptyString.create("E1").get,
      NonEmptyString.create("External E1").get,
      List[Availability](availability),
      List[Role](Supervisor())
    )

    val e2 = External.create(
      NonEmptyString.create("E2").get,
      NonEmptyString.create("External E2").get,
      List[Availability](availability),
      List[Role](Supervisor())
    )

    val vivas = List[Viva](
      Viva.create(
        NonEmptyString.create("Student 0").get,
        NonEmptyString.create("Viva 0").get,
        Jury
          .create(
            president = t3.get,
            adviser = t1.get,
            supervisors = List[Resource](e1.get),
            coAdvisers = List[Resource]()
          )
          .get,
        duration = Duration.create(java.time.Duration.ofMinutes(60)).get
      ),
      Viva.create(
        NonEmptyString.create("Student 1").get,
        NonEmptyString.create("Viva 1").get,
        Jury
          .create(
            president = t1.get,
            adviser = t2.get,
            supervisors = List[Resource](e2.get),
            coAdvisers = List[Resource]()
          )
          .get,
        duration = Duration.create(java.time.Duration.ofMinutes(60)).get
      ),
      Viva.create(
        NonEmptyString.create("Student 2").get,
        NonEmptyString.create("Viva 2").get,
        Jury
          .create(
            president = t2.get,
            adviser = t3.get,
            supervisors = List[Resource](e2.get),
            coAdvisers = List[Resource]()
          )
          .get,
        duration = Duration.create(java.time.Duration.ofMinutes(60)).get
      ),
      Viva.create(
        NonEmptyString.create("Student 3").get,
        NonEmptyString.create("Viva 3").get,
        Jury
          .create(
            president = t3.get,
            adviser = t4.get,
            supervisors = List[Resource](e1.get),
            coAdvisers = List[Resource]()
          )
          .get,
        duration = Duration.create(java.time.Duration.ofMinutes(60)).get
      )
    )

    val scheduledJury = Jury
      .create(
        president = t3.get,
        adviser = t1.get,
        supervisors = List[Resource](e2.get),
        coAdvisers = List[Resource]()
      )
      .get

    val start = LocalDateTime.of(2020, 5, 30, 9, 0, 0)
    val end = start.plusHours(1)
    val scheduledPeriod = Period.create(start, end).get

    val expectedNewAvailability = Availability.create(
      Period
        .create(
          LocalDateTime.of(2020, 5, 30, 10, 0, 0),
          LocalDateTime.of(2020, 5, 30, 10, 0, 0).plusHours(4)
        )
        .get,
      Preference.create(5).get
    )

    //Act

    val newUpdatedVivas =
      ScheduledVivaService.updateVivasAccordingToScheduledVivaPeriod(
        scheduledJury,
        vivas,
        scheduledPeriod
      )

    val updatedAvailabilities = newUpdatedVivas
      .flatMap(x => x.jury.asResourcesSet)
      .flatMap(x => x.availabilities)

    //Assert

    updatedAvailabilities.foreach(x => {
      x.period shouldBe expectedNewAvailability.period
    })
  }
}
