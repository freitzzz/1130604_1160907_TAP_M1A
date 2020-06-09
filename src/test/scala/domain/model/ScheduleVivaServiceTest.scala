package domain.model

import java.time.LocalDateTime

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class ScheduleVivaServiceTest extends AnyFunSuite with Matchers {

  test(
    "the total preference value of a set of resources should be equal to the sum of each resources's individual availabilities preferences"
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
}
