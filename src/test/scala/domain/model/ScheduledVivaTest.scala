package domain.model

import java.time.LocalDateTime

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class ScheduledVivaTest extends AnyFunSuite with Matchers {

  test("scheduled viva time period must comply with jury availabilities") {

    // Arrange

    val student = NonEmptyString.create("Doe").get

    val title =
      NonEmptyString
        .create(
          "Understanding Parallelism Programming with Functional Programming Paradigm"
        )
        .get

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
      .get

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
      .get

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

    val jury = Jury.create(president, adviser, List.empty, List.empty).get

    val viva = Viva.create(student, title, jury).get

    val scheduledVivaStartDateTime = presidentAvailabilityStartDateTime

    val scheduledVivaEndDateTime =
      adviserAvailabilityEndDateTime.plusMinutes(20)

    val scheduledVivaPeriod =
      Period.create(scheduledVivaStartDateTime, scheduledVivaEndDateTime).get

    // Act

    val scheduledViva = ScheduledViva.create(viva, scheduledVivaPeriod)

    // Assert

    scheduledViva.isFailure shouldBe true
  }

  test(
    "if end date time is after start date time && complies jury availabilities, a valid Scheduled Viva can be produced"
  ) {

    // Arrange

    val student = NonEmptyString.create("Doe").get

    val title =
      NonEmptyString
        .create(
          "Understanding Parallelism Programming with Functional Programming Paradigm"
        )
        .get

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
      .get

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
      .get

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

    val jury = Jury.create(president, adviser, List.empty, List.empty).get

    val viva = Viva.create(student, title, jury).get

    val scheduledVivaStartDateTime =
      presidentAvailabilityStartDateTime.plusMinutes(5)

    val scheduledVivaEndDateTime =
      scheduledVivaStartDateTime.plusMinutes(5)

    val scheduledVivaPeriod =
      Period.create(scheduledVivaStartDateTime, scheduledVivaEndDateTime).get

    // Act

    val scheduledViva = ScheduledViva.create(viva, scheduledVivaPeriod)

    // Assert

    scheduledViva.isSuccess shouldBe true
  }

  test(
    "the preference value of a schedule viva must be the sum of the preference of the availability of each resource of the viva jury"
  ) {

    // Arrange

    val student = NonEmptyString.create("Doe").get

    val title =
      NonEmptyString
        .create(
          "Understanding Parallelism Programming with Functional Programming Paradigm"
        )
        .get

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
      .get

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
      .get

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

    val jury = Jury.create(president, adviser, List.empty, List.empty).get

    val viva = Viva.create(student, title, jury).get

    val scheduledVivaStartDateTime =
      presidentAvailabilityStartDateTime.plusMinutes(5)

    val scheduledVivaEndDateTime =
      scheduledVivaStartDateTime.plusMinutes(5)

    val scheduledVivaPeriod =
      Period.create(scheduledVivaStartDateTime, scheduledVivaEndDateTime).get

    val scheduledViva = ScheduledViva
      .create(viva, scheduledVivaPeriod)
      .get

    // Act

    val expectedScheduledVivaPreference = viva.jury.asResourcesSet
      .flatMap(resource => resource.availabilityOn(scheduledVivaPeriod))
      .foldLeft(0)(_ + _.preference.value)

    // Assert

    scheduledViva.scheduledPreference shouldBe expectedScheduledVivaPreference
  }
}
