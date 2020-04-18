package domain.model

import java.time.LocalDateTime

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class AvailabilityTest extends AnyFunSuite with Matchers {

  test("availability preference should not accept negative values") {

    // Arrange

    val start = LocalDateTime.now()
    val end = start.plusMinutes(5)
    val preference = 0

    // Act

    val availability = Availability.create(start, end, preference)

    // Assert

    availability shouldBe None

  }

  test("availability preference should not accept the value 0") {

    // Arrange

    val start = LocalDateTime.now()
    val end = start.plusMinutes(5)
    val preference = 0

    // Act

    val availability = Availability.create(start, end, preference)

    // Assert

    availability shouldBe None

  }

  test("availability preference should not accept the values greater than 10") {

    // Arrange

    val start = LocalDateTime.now()
    val end = start.plusMinutes(5)
    val preference = 11

    // Act

    val availability = Availability.create(start, end, preference)

    // Assert

    availability shouldBe None

  }

  test("start date time cannot be after end date time") {

    // Arrange

    val start = LocalDateTime.now()
    val end = start.minusMinutes(5)
    val preference = 5

    // Act

    val availability = Availability.create(start, end, preference)

    // Assert

    availability shouldBe None

  }

  test(
    "with end date time after start date time and preference ranging values [1-10] a valid availability can be produced"
  ) {

    // Arrange

    val start = LocalDateTime.now()
    val end = start.plusMinutes(5)
    val preference = 5

    // Act

    val availability = Availability.create(start, end, preference)

    // Assert

    availability shouldBe Some(availability.get)

  }

}
