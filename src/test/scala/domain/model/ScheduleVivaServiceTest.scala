package domain.model

import java.time.LocalDateTime

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import scala.util.Try

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
    "if a viva has two resources with two availabilities each of 1 hour and the viva duration is 1 hour, then findPeriodsInWhichVivaCanOccur should return a set with those two availability periods"
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

    findPeriodsInWhichVivaCanOccur shouldBe Set(
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
      .flatMap(x => x.jury.asResourcesSet) //T3, T1, E2
      .filter(x => x.id.s == "T3" || x.id.s == "T1" || x.id.s == "E2")
      .flatMap(x => x.availabilities)

    //Assert

    updatedAvailabilities.foreach(x => {
      x.period shouldBe expectedNewAvailability.period
    })
  }

  test(
    "if a viva has two resources with two availabilities each of 1 hour and the viva duration is 1 hour - 1 second, then findPeriodsInWhichVivaCanOccur should return a set with those availabilities"
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

    val vivaDuration =
      Duration.create(java.time.Duration.ofHours(1).minusSeconds(1)).get

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

    findPeriodsInWhichVivaCanOccur shouldBe Set(periodX, periodY)

  }

  test(
    "if a viva has two resources with two availabilities each of 1 hour and the viva duration is 1 hour + 1 second, then findPeriodsInWhichVivaCanOccur should return an empty set"
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

    val vivaDuration =
      Duration.create(java.time.Duration.ofHours(1).plusSeconds(1)).get

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

    findPeriodsInWhichVivaCanOccur shouldBe Set()

  }

  //This test was based on the control file named 'Custom_valid_agenda_01_in.xml'
  test("given a list of vivas, the maxed availability is found") {

    // Arrange
    val availability1T001 = Availability.create(
      Period
        .create(
          LocalDateTime.of(2020, 5, 30, 9, 30, 0),
          LocalDateTime.of(2020, 5, 30, 9, 30, 0).plusHours(3)
        )
        .get,
      Preference.create(5).get
    )

    val availability2T001 = Availability.create(
      Period
        .create(
          LocalDateTime.of(2020, 5, 30, 13, 30, 0),
          LocalDateTime.of(2020, 5, 30, 13, 30, 0).plusHours(3)
        )
        .get,
      Preference.create(3).get
    )

    val availability1T002 = Availability.create(
      Period
        .create(
          LocalDateTime.of(2020, 5, 30, 10, 30, 0),
          LocalDateTime.of(2020, 5, 30, 10, 30, 0).plusHours(1)
        )
        .get,
      Preference.create(5).get
    )

    val availability2T002 = Availability.create(
      Period
        .create(
          LocalDateTime.of(2020, 5, 30, 14, 30, 0),
          LocalDateTime.of(2020, 5, 30, 14, 30, 0).plusMinutes(90)
        )
        .get,
      Preference.create(5).get
    )

    val availability2T003 = Availability.create(
      Period
        .create(
          LocalDateTime.of(2020, 5, 30, 13, 30, 0),
          LocalDateTime.of(2020, 5, 30, 13, 30, 0).plusMinutes(210)
        )
        .get,
      Preference.create(3).get
    )

    val availability1T003 = Availability.create(
      Period
        .create(
          LocalDateTime.of(2020, 5, 30, 9, 30, 0),
          LocalDateTime.of(2020, 5, 30, 9, 30, 0).plusMinutes(180)
        )
        .get,
      Preference.create(5).get
    )

    val availability1T004 = Availability.create(
      Period
        .create(
          LocalDateTime.of(2020, 5, 30, 10, 30, 0),
          LocalDateTime.of(2020, 5, 30, 10, 30, 0).plusMinutes(60)
        )
        .get,
      Preference.create(5).get
    )

    val availability2T004 = Availability.create(
      Period
        .create(
          LocalDateTime.of(2020, 5, 30, 14, 30, 0),
          LocalDateTime.of(2020, 5, 30, 14, 30, 0).plusMinutes(150)
        )
        .get,
      Preference.create(5).get
    )

    val availability1E001 = Availability.create(
      Period
        .create(
          LocalDateTime.of(2020, 5, 30, 10, 0, 0),
          LocalDateTime.of(2020, 5, 30, 10, 0, 0).plusMinutes(210)
        )
        .get,
      Preference.create(2).get
    )

    val availability2E001 = Availability.create(
      Period
        .create(
          LocalDateTime.of(2020, 5, 30, 15, 30, 0),
          LocalDateTime.of(2020, 5, 30, 15, 30, 0).plusMinutes(150)
        )
        .get,
      Preference.create(5).get
    )

    val availability1E002 = Availability.create(
      Period
        .create(
          LocalDateTime.of(2020, 5, 30, 10, 0, 0),
          LocalDateTime.of(2020, 5, 30, 10, 0, 0).plusMinutes(210)
        )
        .get,
      Preference.create(1).get
    )

    val availability2E002 = Availability.create(
      Period
        .create(
          LocalDateTime.of(2020, 5, 30, 15, 30, 0),
          LocalDateTime.of(2020, 5, 30, 15, 30, 0).plusMinutes(150)
        )
        .get,
      Preference.create(5).get
    )

    val teacher001 = Teacher.create(
      NonEmptyString.create("T001").get,
      NonEmptyString.create("Teacher 001").get,
      List[Availability](availability1T001, availability2T001),
      List[Role](President())
    )

    val teacher002 = Teacher.create(
      NonEmptyString.create("T002").get,
      NonEmptyString.create("Teacher 002").get,
      List[Availability](availability1T002, availability2T002),
      List[Role](Adviser())
    )

    val teacher003 = Teacher.create(
      NonEmptyString.create("T003").get,
      NonEmptyString.create("Teacher 003").get,
      List[Availability](availability1T003, availability2T003),
      List[Role](President())
    )

    val teacher004 = Teacher.create(
      NonEmptyString.create("T004").get,
      NonEmptyString.create("Teacher 004").get,
      List[Availability](availability1T004, availability2T004),
      List[Role](Adviser())
    )

    val external001 = External.create(
      NonEmptyString.create("E001").get,
      NonEmptyString.create("External 001").get,
      List[Availability](availability1E001, availability2E001),
      List[Role](Supervisor())
    )

    val external002 = External.create(
      NonEmptyString.create("E002").get,
      NonEmptyString.create("External 002").get,
      List[Availability](availability1E002, availability2E002),
      List[Role](Supervisor())
    )

    val vivas = List[Viva](
      Viva.create(
        NonEmptyString.create("Student 001").get,
        NonEmptyString.create("Title 1").get,
        Jury
          .create(
            president = teacher001.get,
            adviser = teacher002.get,
            supervisors = List[Resource](external001.get),
            coAdvisers = List[Resource]()
          )
          .get,
        duration = Duration.create(java.time.Duration.ofMinutes(60)).get
      ),
      Viva.create(
        NonEmptyString.create("Student 002").get,
        NonEmptyString.create("Title 2").get,
        Jury
          .create(
            president = teacher003.get,
            adviser = teacher004.get,
            supervisors = List[Resource](external002.get),
            coAdvisers = List[Resource]()
          )
          .get,
        duration = Duration.create(java.time.Duration.ofMinutes(60)).get
      )
    )
    // Act
    val scheduledVivas =
      ScheduledVivaService.ScheduleVivasIndividually(vivas)

    val preferencesFromScheduledVivas =
      scheduledVivas.map(x => x.get.scheduledPreference).sum

    // Assert
    preferencesFromScheduledVivas shouldBe 25
  }

  test(
    "given a list of vivas in which at most one resource of a viva jury does not have compatible availabilities, then the returning list contains a Failure of Schedule"
  ) {

    // Arrange
    val availability1T001 = Availability.create(
      Period
        .create(
          LocalDateTime.of(2020, 5, 30, 9, 30, 0),
          LocalDateTime.of(2020, 5, 30, 9, 30, 0).plusHours(3)
        )
        .get,
      Preference.create(5).get
    )

    val availability2T001 = Availability.create(
      Period
        .create(
          LocalDateTime.of(2020, 5, 30, 13, 30, 0),
          LocalDateTime.of(2020, 5, 30, 13, 30, 0).plusHours(3)
        )
        .get,
      Preference.create(3).get
    )

    val availability1T002 = Availability.create(
      Period
        .create(
          LocalDateTime.of(2020, 5, 30, 10, 30, 0),
          LocalDateTime.of(2020, 5, 30, 10, 30, 0).plusHours(1)
        )
        .get,
      Preference.create(5).get
    )

    val availability2T002 = Availability.create(
      Period
        .create(
          LocalDateTime.of(2020, 5, 30, 14, 30, 0),
          LocalDateTime.of(2020, 5, 30, 14, 30, 0).plusMinutes(90)
        )
        .get,
      Preference.create(5).get
    )

    val availability2T003 = Availability.create(
      Period
        .create(
          LocalDateTime.of(2020, 5, 30, 13, 30, 0),
          LocalDateTime.of(2020, 5, 30, 13, 30, 0).plusMinutes(210)
        )
        .get,
      Preference.create(3).get
    )

    val availability1T003 = Availability.create(
      Period
        .create(
          LocalDateTime.of(2020, 5, 30, 9, 30, 0),
          LocalDateTime.of(2020, 5, 30, 9, 30, 0).plusMinutes(180)
        )
        .get,
      Preference.create(5).get
    )

    val availability1T004 = Availability.create(
      Period
        .create(
          LocalDateTime.of(2020, 5, 30, 10, 30, 0),
          LocalDateTime.of(2020, 5, 30, 10, 30, 0).plusMinutes(60)
        )
        .get,
      Preference.create(5).get
    )

    val availability2T004 = Availability.create(
      Period
        .create(
          LocalDateTime.of(2020, 5, 30, 14, 30, 0),
          LocalDateTime.of(2020, 5, 30, 14, 30, 0).plusMinutes(150)
        )
        .get,
      Preference.create(5).get
    )

    val availability1E001 = Availability.create(
      Period
        .create(
          LocalDateTime.of(2020, 5, 30, 10, 0, 0),
          LocalDateTime.of(2020, 5, 30, 10, 0, 0).plusMinutes(210)
        )
        .get,
      Preference.create(2).get
    )

    val availability2E001 = Availability.create(
      Period
        .create(
          LocalDateTime.of(2020, 5, 30, 15, 30, 0),
          LocalDateTime.of(2020, 5, 30, 15, 30, 0).plusMinutes(150)
        )
        .get,
      Preference.create(5).get
    )

    val availability1E002 = Availability.create(
      Period
        .create(
          LocalDateTime.of(2020, 5, 30, 10, 0, 0),
          LocalDateTime.of(2020, 5, 30, 10, 0, 0).plusMinutes(59)
        )
        .get,
      Preference.create(1).get
    )

    val availability2E002 = Availability.create(
      Period
        .create(
          LocalDateTime.of(2020, 5, 30, 15, 30, 0),
          LocalDateTime.of(2020, 5, 30, 15, 30, 0).plusMinutes(25)
        )
        .get,
      Preference.create(5).get
    )

    val teacher001 = Teacher.create(
      NonEmptyString.create("T001").get,
      NonEmptyString.create("Teacher 001").get,
      List[Availability](availability1T001, availability2T001),
      List[Role](President())
    )

    val teacher002 = Teacher.create(
      NonEmptyString.create("T002").get,
      NonEmptyString.create("Teacher 002").get,
      List[Availability](availability1T002, availability2T002),
      List[Role](Adviser())
    )

    val teacher003 = Teacher.create(
      NonEmptyString.create("T003").get,
      NonEmptyString.create("Teacher 003").get,
      List[Availability](availability1T003, availability2T003),
      List[Role](President())
    )

    val teacher004 = Teacher.create(
      NonEmptyString.create("T004").get,
      NonEmptyString.create("Teacher 004").get,
      List[Availability](availability1T004, availability2T004),
      List[Role](Adviser())
    )

    val external001 = External.create(
      NonEmptyString.create("E001").get,
      NonEmptyString.create("External 001").get,
      List[Availability](availability1E001, availability2E001),
      List[Role](Supervisor())
    )

    val external002 = External.create(
      NonEmptyString.create("E002").get,
      NonEmptyString.create("External 002").get,
      List[Availability](availability1E002, availability2E002),
      List[Role](Supervisor())
    )

    val vivas = List[Viva](
      Viva.create(
        NonEmptyString.create("Student 001").get,
        NonEmptyString.create("Title 1").get,
        Jury
          .create(
            president = teacher001.get,
            adviser = teacher002.get,
            supervisors = List[Resource](external001.get),
            coAdvisers = List[Resource]()
          )
          .get,
        duration = Duration.create(java.time.Duration.ofMinutes(60)).get
      ),
      Viva.create(
        NonEmptyString.create("Student 002").get,
        NonEmptyString.create("Title 2").get,
        Jury
          .create(
            president = teacher003.get,
            adviser = teacher004.get,
            supervisors = List[Resource](external002.get),
            coAdvisers = List[Resource]()
          )
          .get,
        duration = Duration.create(java.time.Duration.ofMinutes(60)).get
      )
    )
    // Act
    val scheduledVivas =
      ScheduledVivaService.ScheduleVivasIndividually(vivas)

    val scheduledVivasContainsFailure = scheduledVivas.exists(_.isFailure)

    // Assert
    scheduledVivasContainsFailure shouldBe true
  }

  test(
    "given a list of vivas which resources have groups of availabilities that lead to same maximum scheduled preference, then the group of availabilities that starts earlier is chosen"
  ) {

    // Arrange
    val availabilityWithMinimumOfPreference = Availability.create(
      Period
        .create(
          LocalDateTime.of(2020, 5, 30, 9, 30, 0),
          LocalDateTime.of(2020, 5, 30, 10, 30, 0)
        )
        .get,
      Preference.create(2).get
    )

    val availabilityWithMaximumOfPreference = Availability.create(
      Period
        .create(
          LocalDateTime.of(2020, 5, 30, 12, 30, 0),
          LocalDateTime.of(2020, 5, 30, 13, 30, 0)
        )
        .get,
      Preference.create(5).get
    )

    val availabilityWithMaximumOfPreferenceAndStartsEarlier =
      Availability.create(
        Period
          .create(
            LocalDateTime.of(2020, 5, 30, 10, 30, 0),
            LocalDateTime.of(2020, 5, 30, 11, 30, 0)
          )
          .get,
        Preference.create(5).get
      )

    val teacher001 = Teacher.create(
      NonEmptyString.create("T001").get,
      NonEmptyString.create("Teacher 001").get,
      List[Availability](
        availabilityWithMinimumOfPreference,
        availabilityWithMaximumOfPreference,
        availabilityWithMaximumOfPreferenceAndStartsEarlier
      ),
      List[Role](President())
    )

    val teacher002 = Teacher.create(
      NonEmptyString.create("T002").get,
      NonEmptyString.create("Teacher 002").get,
      List[Availability](
        availabilityWithMinimumOfPreference,
        availabilityWithMaximumOfPreference,
        availabilityWithMaximumOfPreferenceAndStartsEarlier
      ),
      List[Role](Adviser())
    )

    val teacher003 = Teacher.create(
      NonEmptyString.create("T003").get,
      NonEmptyString.create("Teacher 003").get,
      List[Availability](
        availabilityWithMinimumOfPreference,
        availabilityWithMaximumOfPreference,
        availabilityWithMaximumOfPreferenceAndStartsEarlier
      ),
      List[Role](President())
    )

    val teacher004 = Teacher.create(
      NonEmptyString.create("T004").get,
      NonEmptyString.create("Teacher 004").get,
      List[Availability](
        availabilityWithMinimumOfPreference,
        availabilityWithMaximumOfPreference,
        availabilityWithMaximumOfPreferenceAndStartsEarlier
      ),
      List[Role](Adviser())
    )

    val external001 = External.create(
      NonEmptyString.create("E001").get,
      NonEmptyString.create("External 001").get,
      List[Availability](
        availabilityWithMinimumOfPreference,
        availabilityWithMaximumOfPreference,
        availabilityWithMaximumOfPreferenceAndStartsEarlier
      ),
      List[Role](Supervisor())
    )

    val external002 = External.create(
      NonEmptyString.create("E002").get,
      NonEmptyString.create("External 002").get,
      List[Availability](
        availabilityWithMinimumOfPreference,
        availabilityWithMaximumOfPreference,
        availabilityWithMaximumOfPreferenceAndStartsEarlier
      ),
      List[Role](Supervisor())
    )

    val vivaDuration = Duration.create(java.time.Duration.ofHours(1)).get

    val viva1 = Viva.create(
      NonEmptyString.create("Student 001").get,
      NonEmptyString.create("Title 1").get,
      Jury
        .create(
          president = teacher001.get,
          adviser = teacher002.get,
          supervisors = List[Resource](external001.get),
          coAdvisers = List[Resource]()
        )
        .get,
      duration = vivaDuration
    )

    val viva2 = Viva.create(
      NonEmptyString.create("Student 002").get,
      NonEmptyString.create("Title 2").get,
      Jury
        .create(
          president = teacher003.get,
          adviser = teacher004.get,
          supervisors = List[Resource](external002.get),
          coAdvisers = List[Resource]()
        )
        .get,
      duration = vivaDuration
    )

    val desiredScheduledVivaPeriod =
      availabilityWithMaximumOfPreferenceAndStartsEarlier.period

    val desiredScheduleOfVivas = List[Try[ScheduledViva]](
      ScheduledViva.create(viva1, desiredScheduledVivaPeriod),
      ScheduledViva.create(viva2, desiredScheduledVivaPeriod)
    )

    val vivas = List[Viva](viva1, viva2)

    // Act
    val scheduledVivas =
      ScheduledVivaService.ScheduleVivasIndividually(vivas)

    // Assert
    scheduledVivas shouldBe desiredScheduleOfVivas
  }

}
