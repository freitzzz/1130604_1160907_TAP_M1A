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

  test(
    "given a period from 13:00 to 17:00 and another from 14:00 to 16:00, the overlap should return true"
  ) {
    // Arrange

    val start1 = LocalDateTime.of(2020, 5, 20, 13, 0, 0)
    val end1 = LocalDateTime.of(2020, 5, 20, 17, 0, 0)
    //val end = start.plusMinutes(5)

    val start2 = LocalDateTime.of(2020, 5, 20, 14, 0, 0)
    val end2 = LocalDateTime.of(2020, 5, 20, 16, 0, 0)

    val periodX = Period.create(start1, end1).get

    val periodY = Period.create(start2, end2).get

    // Act

    val overlaps = periodX.overlaps(periodY)

    // Assert

    overlaps shouldBe true
  }

  test(
    "given a period from 14:00 to 16:00 and another from 13:00 to 17:00, the overlap should return true"
  ) {
    // Arrange

    val start1 = LocalDateTime.of(2020, 5, 20, 14, 0, 0)
    val end1 = LocalDateTime.of(2020, 5, 20, 16, 0, 0)

    val start2 = LocalDateTime.of(2020, 5, 20, 13, 0, 0)
    val end2 = LocalDateTime.of(2020, 5, 20, 17, 0, 0)

    val periodX = Period.create(start1, end1).get

    val periodY = Period.create(start2, end2).get

    // Act

    val overlaps = periodX.overlaps(periodY)

    // Assert

    overlaps shouldBe true
  }

  test(
    "given a period from 13:00 to 17:00 and another from 13:00 to 17:00, the overlap should return true"
  ) {
    // Arrange

    val start1 = LocalDateTime.of(2020, 5, 20, 13, 0, 0)
    val end1 = LocalDateTime.of(2020, 5, 20, 17, 0, 0)

    val start2 = LocalDateTime.of(2020, 5, 20, 13, 0, 0)
    val end2 = LocalDateTime.of(2020, 5, 20, 17, 0, 0)

    val periodX = Period.create(start1, end1).get

    val periodY = Period.create(start2, end2).get

    // Act

    val overlaps = periodX.overlaps(periodY)

    // Assert

    overlaps shouldBe true
  }

  test(
    "given a period from 13:00 to 17:00 and another from 13:00 to 18:00, the overlap should return true"
  ) {
    // Arrange

    val start1 = LocalDateTime.of(2020, 5, 20, 13, 0, 0)
    val end1 = LocalDateTime.of(2020, 5, 20, 17, 0, 0)

    val start2 = LocalDateTime.of(2020, 5, 20, 13, 0, 0)
    val end2 = LocalDateTime.of(2020, 5, 20, 18, 0, 0)

    val periodX = Period.create(start1, end1).get

    val periodY = Period.create(start2, end2).get

    // Act

    val overlaps = periodX.overlaps(periodY)

    // Assert

    overlaps shouldBe true
  }
}
