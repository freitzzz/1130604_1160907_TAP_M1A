package domain.model

import java.time.LocalDateTime

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class ScheduledVivaTest extends AnyFunSuite with Matchers {

  test("start date time cannot be after end date time") {

    // Arrange

    val student = "Doe"

    val title =
      "Understanding Parallelism Programming with Functional Programming Paradigm"

    val presidentAvailabilityStartDateTime = LocalDateTime.now()

    val presidentAvailabilityEndDateTime =
      presidentAvailabilityStartDateTime.plusMinutes(10)

    val presidentAvailabilityPreference = 5

    val presidentAvailability = Availability
      .create(
        presidentAvailabilityStartDateTime,
        presidentAvailabilityEndDateTime,
        presidentAvailabilityPreference
      )
      .get

    val adviserAvailabilityStartDateTime =
      presidentAvailabilityStartDateTime.plusMinutes(5)

    val adviserAvailabilityEndDateTime =
      presidentAvailabilityStartDateTime.plusMinutes(15)

    val adviserAvailabilityPreference = 3

    val adviserAvailability = Availability
      .create(
        adviserAvailabilityStartDateTime,
        adviserAvailabilityEndDateTime,
        adviserAvailabilityPreference
      )
      .get

    val president = Teacher
      .create("1", "John", List(presidentAvailability), List(President()))
      .get

    val adviser =
      Teacher.create("2", "Doe", List(adviserAvailability), List(Adviser())).get

    val jury = Jury.create(president, adviser, List.empty, List.empty).get

    val viva = Viva.create(student, title, jury).get

    val scheduledVivaStartDateTime = presidentAvailabilityEndDateTime

    val scheduledVivaEndDateTime = presidentAvailabilityStartDateTime

    // Act

    val scheduledViva = ScheduledViva.create(
      viva,
      scheduledVivaStartDateTime,
      scheduledVivaEndDateTime
    )

    // Assert

    scheduledViva.isFailure shouldBe true

  }

  test("scheduled viva time period must comply with jury availabilities") {

    // Arrange

    val student = "Doe"

    val title =
      "Understanding Parallelism Programming with Functional Programming Paradigm"

    val presidentAvailabilityStartDateTime = LocalDateTime.now()

    val presidentAvailabilityEndDateTime =
      presidentAvailabilityStartDateTime.plusMinutes(10)

    val presidentAvailabilityPreference = 5

    val presidentAvailability = Availability
      .create(
        presidentAvailabilityStartDateTime,
        presidentAvailabilityEndDateTime,
        presidentAvailabilityPreference
      )
      .get

    val adviserAvailabilityStartDateTime =
      presidentAvailabilityStartDateTime.plusMinutes(5)

    val adviserAvailabilityEndDateTime =
      presidentAvailabilityStartDateTime.plusMinutes(15)

    val adviserAvailabilityPreference = 3

    val adviserAvailability = Availability
      .create(
        adviserAvailabilityStartDateTime,
        adviserAvailabilityEndDateTime,
        adviserAvailabilityPreference
      )
      .get

    val president = Teacher
      .create("1", "John", List(presidentAvailability), List(President()))
      .get

    val adviser =
      Teacher.create("2", "Doe", List(adviserAvailability), List(Adviser())).get

    val jury = Jury.create(president, adviser, List.empty, List.empty).get

    val viva = Viva.create(student, title, jury).get

    val scheduledVivaStartDateTime = presidentAvailabilityStartDateTime

    val scheduledVivaEndDateTime =
      adviserAvailabilityEndDateTime.plusMinutes(20)

    // Act

    val scheduledViva = ScheduledViva.create(
      viva,
      scheduledVivaStartDateTime,
      scheduledVivaEndDateTime
    )

    // Assert

    scheduledViva.isFailure shouldBe true
  }

  test(
    "if end date time is after start date time && complies jury availabilities, a valid Scheduled Viva can be produced"
  ) {

    // Arrange

    val student = "Doe"

    val title =
      "Understanding Parallelism Programming with Functional Programming Paradigm"

    val presidentAvailabilityStartDateTime = LocalDateTime.now()

    val presidentAvailabilityEndDateTime =
      presidentAvailabilityStartDateTime.plusMinutes(10)

    val presidentAvailabilityPreference = 5

    val presidentAvailability = Availability
      .create(
        presidentAvailabilityStartDateTime,
        presidentAvailabilityEndDateTime,
        presidentAvailabilityPreference
      )
      .get

    val adviserAvailabilityStartDateTime =
      presidentAvailabilityStartDateTime.plusMinutes(5)

    val adviserAvailabilityEndDateTime =
      presidentAvailabilityStartDateTime.plusMinutes(15)

    val adviserAvailabilityPreference = 3

    val adviserAvailability = Availability
      .create(
        adviserAvailabilityStartDateTime,
        adviserAvailabilityEndDateTime,
        adviserAvailabilityPreference
      )
      .get

    val president = Teacher
      .create("1", "John", List(presidentAvailability), List(President()))
      .get

    val adviser =
      Teacher.create("2", "Doe", List(adviserAvailability), List(Adviser())).get

    val jury = Jury.create(president, adviser, List.empty, List.empty).get

    val viva = Viva.create(student, title, jury).get

    val scheduledVivaStartDateTime =
      presidentAvailabilityStartDateTime.plusMinutes(5)

    val scheduledVivaEndDateTime =
      scheduledVivaStartDateTime.plusMinutes(5)

    // Act

    val scheduledViva = ScheduledViva.create(
      viva,
      scheduledVivaStartDateTime,
      scheduledVivaEndDateTime
    )

    // Assert

    scheduledViva.isSuccess shouldBe true
  }

  test(
    "the preference value of a schedule viva must be the sum of the preference of the availability of each resource of the viva jury"
  ) {

    // Arrange

    val student = "Doe"

    val title =
      "Understanding Parallelism Programming with Functional Programming Paradigm"

    val presidentAvailabilityStartDateTime = LocalDateTime.now()

    val presidentAvailabilityEndDateTime =
      presidentAvailabilityStartDateTime.plusMinutes(10)

    val presidentAvailabilityPreference = 5

    val presidentAvailability = Availability
      .create(
        presidentAvailabilityStartDateTime,
        presidentAvailabilityEndDateTime,
        presidentAvailabilityPreference
      )
      .get

    val adviserAvailabilityStartDateTime =
      presidentAvailabilityStartDateTime.plusMinutes(5)

    val adviserAvailabilityEndDateTime =
      presidentAvailabilityStartDateTime.plusMinutes(15)

    val adviserAvailabilityPreference = 3

    val adviserAvailability = Availability
      .create(
        adviserAvailabilityStartDateTime,
        adviserAvailabilityEndDateTime,
        adviserAvailabilityPreference
      )
      .get

    val president = Teacher
      .create("1", "John", List(presidentAvailability), List(President()))
      .get

    val adviser =
      Teacher.create("2", "Doe", List(adviserAvailability), List(Adviser())).get

    val jury = Jury.create(president, adviser, List.empty, List.empty).get

    val viva = Viva.create(student, title, jury).get

    val scheduledVivaStartDateTime =
      presidentAvailabilityStartDateTime.plusMinutes(5)

    val scheduledVivaEndDateTime =
      scheduledVivaStartDateTime.plusMinutes(5)

    val scheduledViva = ScheduledViva
      .create(viva, scheduledVivaStartDateTime, scheduledVivaEndDateTime)
      .get

    // Act

    val expectedScheduledVivaPreference = viva.jury.asResourcesSet
      .flatMap(
        resource =>
          resource.availabilityOn(
            scheduledVivaStartDateTime,
            scheduledVivaEndDateTime
        )
      )
      .foldLeft(0)(_ + _.preference)

    // Assert

    scheduledViva.scheduledPreference shouldBe expectedScheduledVivaPreference
  }
}
