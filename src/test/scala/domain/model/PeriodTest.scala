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

}
