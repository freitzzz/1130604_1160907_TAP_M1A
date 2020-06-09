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
}
