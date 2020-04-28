package domain.model

import java.time

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class DurationTest extends AnyFunSuite with Matchers {

  test("duration cannot be lower than zero") {

    // Arrange

    val timeDuration = time.Duration.ofSeconds(-1)

    // Act

    val duration = Duration.create(timeDuration)

    // Assert

    duration.isFailure shouldBe true

  }

  test("duration cannot be equal to zero") {

    // Arrange

    val timeDuration = time.Duration.ofSeconds(0)

    // Act

    val duration = Duration.create(timeDuration)

    // Assert

    duration.isFailure shouldBe true

  }

  test(
    "given a time duration that is higher than zero, then a valid duration can be produced"
  ) {

    // Arrange

    val timeDuration = time.Duration.ofSeconds(1)

    // Act

    val duration = Duration.create(timeDuration)

    // Assert

    duration.isSuccess shouldBe true

  }

}
