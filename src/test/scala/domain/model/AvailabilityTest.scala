package domain.model

import java.time.LocalDateTime

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class AvailabilityTest extends AnyFunSuite with Matchers {

  test("availability preference should not accept negative values") {

    // Arrange

    val start = LocalDateTime.now() //
    val end = start.plusMinutes(5)
    val preference = 0

    // Act

    val availability = Availability.create(start, end, preference)

    // Assert

    availability.isFailure shouldBe true

  }

  test("availability preference should not accept the value 0") {

    // Arrange

    val start = LocalDateTime.now()
    val end = start.plusMinutes(5)
    val preference = 0

    // Act

    val availability = Availability.create(start, end, preference)

    // Assert

    availability.isFailure shouldBe true

  }

  test("availability preference should not accept the values greater than 10") {

    // Arrange

    val start = LocalDateTime.now()
    val end = start.plusMinutes(5)
    val preference = 11

    // Act

    val availability = Availability.create(start, end, preference)

    // Assert

    availability.isFailure shouldBe true

  }

  test("start date time cannot be after end date time") {

    // Arrange

    val start = LocalDateTime.now()
    val end = start.minusMinutes(5)
    val preference = 5

    // Act

    val availability = Availability.create(start, end, preference)

    // Assert

    availability.isFailure shouldBe true

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

    availability.isSuccess shouldBe true

  }

  test(
    "hashcode should be the sum of availability start and end date time hash code plus preference value"
  ) {

    // Arrange

    val start = LocalDateTime.now()
    val end = start.plusMinutes(5)
    val preference = 5
    val expectedHashCode = start.hashCode() + end.hashCode() + preference

    // Act

    val availabilityHashCode =
      Availability.create(start, end, preference).get.hashCode()

    // Assert

    availabilityHashCode shouldEqual expectedHashCode

  }

  test(
    "equality should be given by objects hash code, so an availability instance X with different hash code than availability instance Y should not be equal to instance Y"
  ) {

    // Arrange

    val startX = LocalDateTime.now()
    val endX = startX.plusMinutes(5)
    val preferenceX = 5

    val startY = startX.plusMinutes(10)
    val endY = startY.plusMinutes(5)
    val preferenceY = 5

    // Act

    val availabilityX = Availability.create(startX, endX, preferenceX).get

    val availabilityY = Availability.create(startY, endY, preferenceY).get

    val availabilityXHashCode = availabilityX.hashCode()

    val availabilityYHashCode = availabilityY.hashCode()

    val equality = availabilityX.equals(availabilityY)

    // Assert

    availabilityXHashCode shouldNot equal(availabilityYHashCode)

    equality shouldBe false

  }

  test(
    "equality should be given by objects hash code, so an availability instance X with equal hash code than availability instance Y should be equal to instance Y"
  ) {

    // Arrange

    val startX = LocalDateTime.now()
    val endX = startX.plusMinutes(5)
    val preferenceX = 5

    val startY = startX
    val endY = startY.plusMinutes(5)
    val preferenceY = 5

    // Act

    val availabilityX = Availability.create(startX, endX, preferenceX).get

    val availabilityY = Availability.create(startY, endY, preferenceY).get

    val availabilityXHashCode = availabilityX.hashCode()

    val availabilityYHashCode = availabilityY.hashCode()

    val equality = availabilityX.equals(availabilityY)

    // Assert

    availabilityXHashCode shouldEqual availabilityYHashCode

    equality shouldBe true

  }

}
