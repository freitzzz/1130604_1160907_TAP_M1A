package domain.model

import java.time.LocalDateTime

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class PeriodTest extends AnyFunSuite with Matchers {

  test("start date time cannot be after end date time") {

    // Arrange

    val start = LocalDateTime.now()
    val end = start.minusMinutes(5)

    // Act

    val period = Period.create(start, end)

    // Assert

    period.isFailure shouldBe true

  }

  test(
    "if the period start date time is before the period end date time, then a valid period can be produced"
  ) {

    // Arrange

    val start = LocalDateTime.now()
    val end = start.plusMinutes(5)

    // Act

    val period = Period.create(start, end)

    // Assert

    period.isSuccess shouldBe true

  }

  test(
    "given a period that overlaps with another period of time, overlaps method returns true"
  ) {

    // Arrange

    val start = LocalDateTime.now()
    val end = start.plusMinutes(5)

    val periodX = Period.create(start, end).get

    val periodY = Period.create(start.plusMinutes(2), end.plusMinutes(1)).get

    // Act

    val overlaps = periodX.overlaps(periodY)

    // Assert

    overlaps shouldBe true

  }

  test(
    "given a period that does not overlap with another period of time, overlaps method returns false"
  ) {

    // Arrange

    val start = LocalDateTime.now()
    val end = start.plusMinutes(5)

    val periodX = Period.create(start, end).get

    val periodY = Period.create(start.plusMinutes(6), end.plusMinutes(2)).get

    // Act

    val overlaps = periodX.overlaps(periodY)

    // Assert

    overlaps shouldBe false

  }

}
