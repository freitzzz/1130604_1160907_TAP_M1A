package domain.model

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class NonEmptyStringTest extends AnyFunSuite with Matchers {

  test("String is null NonEmptyString should fail") {

    // Arrange

    val stringToTest = null

    // Act

    val nonEmptyString = NonEmptyString.create(stringToTest)

    // Assert

    nonEmptyString.isFailure shouldBe true
  }

  test("String is empty NonEmptyString should fail") {

    // Arrange

    val stringToTest = ""

    // Act

    val nonEmptyString = NonEmptyString.create(stringToTest)

    // Assert

    nonEmptyString.isFailure shouldBe true
  }

  test("String is valid NonEmptyString should succeed") {

    // Arrange

    val stringToTest = "ArminVanBuuren"

    // Act

    val nonEmptyString = NonEmptyString.create(stringToTest)

    // Assert

    nonEmptyString.isSuccess shouldBe true
  }
}
