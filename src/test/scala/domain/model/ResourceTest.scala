package domain.model

import java.time.LocalDateTime

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class ResourceTest extends AnyFunSuite with Matchers {

  test("a resource is not valid if it contains duplicate availabilities") {

    // Arrange

    val id = "1"

    val name = "John"

    val availability = Availability
      .create(LocalDateTime.now(), LocalDateTime.now().plusMinutes(10), 5)
      .get

    val duplicateAvailability = availability

    val role = President()

    val availabilities = List[Availability](availability, duplicateAvailability)

    val roles = List[Role](role)

    // Act

    val isValid = Resource.validResource(id, name, availabilities, roles)

    // Assert

    isValid shouldBe false

  }

  test("a resource is not valid if it does not have roles") {

    // Arrange

    val id = "1"

    val name = "John"

    val availability = Availability
      .create(LocalDateTime.now(), LocalDateTime.now().plusMinutes(10), 5)
      .get

    val availabilities = List[Availability](availability)

    val roles = List[Role]()

    // Act

    val isValid = Resource.validResource(id, name, availabilities, roles)

    // Assert

    isValid shouldBe false

  }

  test("a resource is not valid if it contains duplicate roles") {

    // Arrange

    val id = "1"

    val name = "John"

    val availability = Availability
      .create(LocalDateTime.now(), LocalDateTime.now().plusMinutes(10), 5)
      .get

    val role = President()

    val duplicateRole = President()

    val availabilities = List[Availability](availability)

    val roles = List[Role](role, duplicateRole)

    // Act

    val isValid = Resource.validResource(id, name, availabilities, roles)

    // Assert

    isValid shouldBe false

  }

  test(
    "a resource is valid if it does not contain duplicate availabilities nor duplicate roles"
  ) {

    // Arrange

    val id = "1"

    val name = "John"

    val availability = Availability
      .create(LocalDateTime.now(), LocalDateTime.now().plusMinutes(10), 5)
      .get

    val role = President()

    val availabilities = List[Availability](availability)

    val roles = List[Role](role)

    // Act

    val isValid = Resource.validResource(id, name, availabilities, roles)

    // Assert

    isValid shouldBe true

  }

  test(
    "a resource is valid if it does not contain availabilities and duplicate roles"
  ) {

    // Arrange

    val id = "1"

    val name = "John"

    val role = President()

    val availabilities = List[Availability]()

    val roles = List[Role](role)

    // Act

    val isValid = Resource.validResource(id, name, availabilities, roles)

    // Assert

    isValid shouldBe true

  }

  test("a teacher cannot have the supervisor role") {

    // Arrange

    val id = "1"

    val name = "John"

    val role = Supervisor()

    val availabilities = List[Availability]()

    val roles = List[Role](role)

    // Act

    val teacher = Teacher.create(id, name, availabilities, roles)

    // Assert

    teacher shouldBe None

  }

  test("with a role different than Supervisor, a Teacher can be produced") {

    // Arrange

    val id = "1"

    val name = "John"

    val role = President()

    val availabilities = List[Availability]()

    val roles = List[Role](role)

    // Act

    val teacher = Teacher.create(id, name, availabilities, roles)

    // Assert

    teacher shouldBe Some(teacher.get)

  }

  test("an external cannot have the president role") {

    // Arrange

    val id = "1"

    val name = "Mary"

    val role = President()

    val availabilities = List[Availability]()

    val roles = List[Role](role)

    // Act

    val external = External.create(id, name, availabilities, roles)

    // Assert

    external shouldBe None

  }

  test("an external cannot have the adviser role") {

    // Arrange

    val id = "1"

    val name = "Mary"

    val role = Adviser()

    val availabilities = List[Availability]()

    val roles = List[Role](role)

    // Act

    val external = External.create(id, name, availabilities, roles)

    // Assert

    external shouldBe None

  }

  test(
    "with a role different than President and Adviser, an External can be produced"
  ) {

    // Arrange

    val id = "1"

    val name = "John"

    val roleX = CoAdviser()

    val roleY = Supervisor()

    val availabilities = List[Availability]()

    val roles = List[Role](roleX, roleY)

    // Act

    val external = External.create(id, name, availabilities, roles)

    // Assert

    external shouldBe Some(external.get)

  }

  test(
    "hasRole returns false if it does not contain an instance which class is the one provided as argument"
  ) {

    // Arrange

    val id = "1"

    val name = "John"

    val roleX = CoAdviser()

    val roleY = Supervisor()

    val availabilities = List[Availability]()

    val roles = List[Role](roleX, roleY)

    // Act

    val external = External.create(id, name, availabilities, roles).get

    val hasPresidentRole = external.hasRole(President())

    // Assert

    hasPresidentRole shouldBe false

  }

  test(
    "hasRole returns true if it contains an instance which class is the one provided as argument"
  ) {

    // Arrange

    val id = "1"

    val name = "John"

    val roleX = CoAdviser()

    val roleY = Supervisor()

    val availabilities = List[Availability]()

    val roles = List[Role](roleX, roleY)

    // Act

    val external = External.create(id, name, availabilities, roles).get

    val hasPresidentRole = external.hasRole(CoAdviser())

    // Assert

    hasPresidentRole shouldBe true

  }

  test(
    "given a period of time in which the resource is available on, availabilityOn returns the resource availability that complies with the defined period of time"
  ) {

    // Arrange

    val id = "1"

    val name = "John"

    val roleX = CoAdviser()

    val roleY = Supervisor()

    val startDateTimeX = LocalDateTime.now()

    val endDateTimeX = startDateTimeX.plusMinutes(5)

    val preferenceX = 5

    val availabilityX =
      Availability.create(startDateTimeX, endDateTimeX, preferenceX).get

    val startDateTimeY = startDateTimeX.plusMinutes(6)

    val endDateTimeY = startDateTimeY.plusMinutes(5)

    val preferenceY = 5

    val availabilityY =
      Availability.create(startDateTimeY, endDateTimeY, preferenceY).get

    val availabilities = List[Availability](availabilityX, availabilityY)

    val roles = List[Role](roleX, roleY)

    // Act

    val periodWhichResourceIsAvailableOnStart = startDateTimeX.plusMinutes(3)

    val periodWhichResourceIsAvailableOnEnd = endDateTimeX

    val external = External.create(id, name, availabilities, roles).get

    val availabilityOnGivenPeriod = external.availabilityOn(
      periodWhichResourceIsAvailableOnStart,
      periodWhichResourceIsAvailableOnEnd
    )

    val expectedAvailability = availabilityX

    // Assert

    availabilityOnGivenPeriod shouldBe Some(expectedAvailability)

  }

  test(
    "given a period of time in which the resource is not available, availabilityOn returns the resource availability that complies with the defined period of time"
  ) {

    // Arrange

    val id = "1"

    val name = "John"

    val roleX = CoAdviser()

    val roleY = Supervisor()

    val startDateTimeX = LocalDateTime.now()

    val endDateTimeX = startDateTimeX.plusMinutes(5)

    val preferenceX = 5

    val availabilityX =
      Availability.create(startDateTimeX, endDateTimeX, preferenceX).get

    val startDateTimeY = startDateTimeX.plusMinutes(6)

    val endDateTimeY = startDateTimeY.plusMinutes(5)

    val preferenceY = 5

    val availabilityY =
      Availability.create(startDateTimeY, endDateTimeY, preferenceY).get

    val availabilities = List[Availability](availabilityX, availabilityY)

    val roles = List[Role](roleX, roleY)

    // Act

    val periodWhichResourceIsAvailableOnStart = startDateTimeX.plusMinutes(3)

    val periodWhichResourceIsAvailableOnEnd = endDateTimeY

    val external = External.create(id, name, availabilities, roles).get

    val availabilityOnGivenPeriod = external.availabilityOn(
      periodWhichResourceIsAvailableOnStart,
      periodWhichResourceIsAvailableOnEnd
    )

    // Assert

    availabilityOnGivenPeriod shouldBe None

  }

  test(
    "given a period of time in which the resource is available on, isAvailableOn returns boolean true"
  ) {

    // Arrange

    val id = "1"

    val name = "John"

    val roleX = CoAdviser()

    val roleY = Supervisor()

    val startDateTimeX = LocalDateTime.now()

    val endDateTimeX = startDateTimeX.plusMinutes(5)

    val preferenceX = 5

    val availabilityX =
      Availability.create(startDateTimeX, endDateTimeX, preferenceX).get

    val startDateTimeY = startDateTimeX.plusMinutes(6)

    val endDateTimeY = startDateTimeY.plusMinutes(5)

    val preferenceY = 5

    val availabilityY =
      Availability.create(startDateTimeY, endDateTimeY, preferenceY).get

    val availabilities = List[Availability](availabilityX, availabilityY)

    val roles = List[Role](roleX, roleY)

    // Act

    val periodWhichResourceIsAvailableOnStart = startDateTimeX.plusMinutes(3)

    val periodWhichResourceIsAvailableOnEnd = endDateTimeX

    val external = External.create(id, name, availabilities, roles).get

    val isAvailableOnGivenPeriod = external.isAvailableOn(
      periodWhichResourceIsAvailableOnStart,
      periodWhichResourceIsAvailableOnEnd
    )

    // Assert

    isAvailableOnGivenPeriod shouldBe true

  }

  test(
    "given a period of time in which the resource is not available, isAvailableOn returns boolean true"
  ) {

    // Arrange

    val id = "1"

    val name = "John"

    val roleX = CoAdviser()

    val roleY = Supervisor()

    val startDateTimeX = LocalDateTime.now()

    val endDateTimeX = startDateTimeX.plusMinutes(5)

    val preferenceX = 5

    val availabilityX =
      Availability.create(startDateTimeX, endDateTimeX, preferenceX).get

    val startDateTimeY = startDateTimeX.plusMinutes(6)

    val endDateTimeY = startDateTimeY.plusMinutes(5)

    val preferenceY = 5

    val availabilityY =
      Availability.create(startDateTimeY, endDateTimeY, preferenceY).get

    val availabilities = List[Availability](availabilityX, availabilityY)

    val roles = List[Role](roleX, roleY)

    // Act

    val periodWhichResourceIsAvailableOnStart = startDateTimeX.plusMinutes(3)

    val periodWhichResourceIsAvailableOnEnd = endDateTimeY

    val external = External.create(id, name, availabilities, roles).get

    val isAvailableOnGivenPeriod = external.isAvailableOn(
      periodWhichResourceIsAvailableOnStart,
      periodWhichResourceIsAvailableOnEnd
    )

    // Assert

    isAvailableOnGivenPeriod shouldBe false

  }

}
