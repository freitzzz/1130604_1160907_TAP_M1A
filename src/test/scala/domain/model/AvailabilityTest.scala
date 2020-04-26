package domain.model

import java.time.LocalDateTime

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class AvailabilityTest extends AnyFunSuite with Matchers {

  test("hashcode should be the sum of period hash code plus preference value") {

    // Arrange

    val start = LocalDateTime.now()
    val end = start.plusMinutes(5)
    val period = Period.create(start, end).get
    val preference = Preference.create(5).get
    val expectedHashCode = period.hashCode() + preference.value

    // Act

    val availabilityHashCode =
      Availability.create(period, preference).hashCode()

    // Assert

    availabilityHashCode shouldEqual expectedHashCode

  }

  test(
    "equality should be given by objects hash code, so an availability instance X with different hash code than availability instance Y should not be equal to instance Y"
  ) {

    // Arrange

    val startX = LocalDateTime.now()
    val endX = startX.plusMinutes(5)
    val periodX = Period.create(startX, endX).get
    val preferenceX = Preference.create(5).get

    val startY = startX.plusMinutes(10)
    val endY = startY.plusMinutes(5)
    val periodY = Period.create(startY, endY).get
    val preferenceY = Preference.create(5).get

    // Act

    val availabilityX = Availability.create(periodX, preferenceX)

    val availabilityY = Availability.create(periodY, preferenceY)

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
    val periodX = Period.create(startX, endX).get
    val preferenceX = Preference.create(5).get

    val startY = startX
    val endY = startY.plusMinutes(5)
    val periodY = Period.create(startY, endY).get
    val preferenceY = Preference.create(5).get

    // Act

    val availabilityX = Availability.create(periodX, preferenceX)

    val availabilityY = Availability.create(periodY, preferenceY)

    val availabilityXHashCode = availabilityX.hashCode()

    val availabilityYHashCode = availabilityY.hashCode()

    val equality = availabilityX.equals(availabilityY)

    // Assert

    availabilityXHashCode shouldEqual availabilityYHashCode

    equality shouldBe true

  }

}
