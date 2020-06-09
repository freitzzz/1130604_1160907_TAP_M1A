package domain.model

import java.time.LocalDateTime

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class ScheduleVivaServiceTest extends AnyFunSuite with Matchers {

  test(
    "the total preference value of a set of resources should be equal to the sum of each resources's individual availabilities preferences"
<<<<<<< HEAD
  ) {}

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
=======
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
>>>>>>> a0e71b707e421d7b1a3937b0b5ee64529744642f
        List(President())
      )
      .get

<<<<<<< HEAD
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
=======
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
>>>>>>> a0e71b707e421d7b1a3937b0b5ee64529744642f

  }
}
