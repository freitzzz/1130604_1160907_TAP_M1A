package domain.model

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class PreferenceTest extends AnyFunSuite with Matchers {

  test("Preference cannot assume values lower than 1") {

    // Arrange

    val value = 0

    // Act

    val preference = Preference.create(value)

    // Assert

    preference.isFailure shouldBe true

  }

  test("Preference cannot assume values higher than 5") {

    // Arrange

    val value = 6

    // Act

    val preference = Preference.create(value)

    // Assert

    preference.isFailure shouldBe true

  }

  test("given a integer in [1, 5] range, a valid preference can be produced") {

    // Arrange

    val values = List(1, 2, 3, 4, 5)

    // Act

    val preferences = values.map(value => Preference.create(value))

    // Assert

    preferences.forall(preference => preference.isSuccess) shouldBe true

  }

}
