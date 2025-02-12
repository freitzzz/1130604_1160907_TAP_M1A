package domain.model

import java.time.LocalDateTime

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class JuryTest extends AnyFunSuite with Matchers {

  test("jury should not accept to be constituted without president") {

    // Arrange
    val president = Teacher(
      id = NonEmptyString.create("Teacher 001").get,
      name = NonEmptyString.create("Fake Teacher").get,
      availabilities = generateAvailabilities,
      roles = List[Role](generateAdviser())
    )

    val adviser = Teacher(
      id = NonEmptyString.create("Teacher 001").get,
      name = NonEmptyString.create("Fake Teacher").get,
      availabilities = generateAvailabilities,
      roles = List[Role](generateAdviser())
    )

    // Act

    val jury =
      Jury.create(president, adviser, List[Resource](), List[Resource]())

    //Assert

    jury.isFailure shouldBe true
  }

  test("jury should not accept to be constituted without adviser") {

    // Arrange
    val president = Teacher(
      id = NonEmptyString.create("Teacher 001").get,
      name = NonEmptyString.create("Fake Teacher").get,
      availabilities = generateAvailabilities,
      roles = List[Role](generatePresident())
    )

    val adviser = Teacher(
      id = NonEmptyString.create("Teacher 001").get,
      name = NonEmptyString.create("Fake Teacher").get,
      availabilities = generateAvailabilities,
      roles = List[Role](generatePresident())
    )

    // Act

    val jury =
      Jury.create(president, adviser, List[Resource](), List[Resource]())

    //Assert

    jury.isFailure shouldBe true
  }

  test("jury with supervisors should all be supervisors") {

    // Arrange
    val president = Teacher(
      id = NonEmptyString.create("Teacher 001").get,
      name = NonEmptyString.create("Fake Teacher").get,
      availabilities = generateAvailabilities,
      roles = List[Role](generatePresident())
    )

    val adviser = Teacher(
      id = NonEmptyString.create("Teacher 001").get,
      name = NonEmptyString.create("Fake Teacher").get,
      availabilities = generateAvailabilities,
      roles = List[Role](generateAdviser())
    )

    val supervisor = External(
      id = NonEmptyString.create("Teacher 001").get,
      name = NonEmptyString.create("Fake Teacher").get,
      availabilities = generateAvailabilities,
      roles = List[Role](generateAdviser())
    )

    // Act

    val jury =
      Jury.create(
        president,
        adviser,
        List[Resource](supervisor),
        List[Resource]()
      )

    //Assert

    jury.isFailure shouldBe true
  }

  test("jury with coadvisers should all be coadvisers") {

    // Arrange
    val president = Teacher(
      id = NonEmptyString.create("Teacher 001").get,
      name = NonEmptyString.create("Fake Teacher").get,
      availabilities = generateAvailabilities,
      roles = List[Role](generatePresident())
    )

    val adviser = Teacher(
      id = NonEmptyString.create("Teacher 001").get,
      name = NonEmptyString.create("Fake Teacher").get,
      availabilities = generateAvailabilities,
      roles = List[Role](generateAdviser())
    )

    val coAdviser = External(
      id = NonEmptyString.create("Teacher 001").get,
      name = NonEmptyString.create("Fake Teacher").get,
      availabilities = generateAvailabilities,
      roles = List[Role](generateAdviser())
    )

    // Act

    val jury =
      Jury.create(
        president,
        adviser,
        List[Resource](),
        List[Resource](coAdviser)
      )

    //Assert

    jury.isFailure shouldBe true
  }

  test("jury with duplicated elements should not be constituted") {

    // Arrange
    val president = Teacher(
      id = NonEmptyString.create("Teacher 001").get,
      name = NonEmptyString.create("Fake Teacher").get,
      availabilities = generateAvailabilities,
      roles = List[Role](generatePresident(), generateAdviser())
    )

    val adviser = Teacher(
      id = NonEmptyString.create("Teacher 001").get,
      name = NonEmptyString.create("Fake Teacher").get,
      availabilities = generateAvailabilities,
      roles = List[Role](generatePresident(), generateAdviser())
    )

    val supervisor = External(
      id = NonEmptyString.create("Teacher 001").get,
      name = NonEmptyString.create("Fake Teacher").get,
      availabilities = generateAvailabilities,
      roles = List[Role](generateSupervisor())
    )

    val coAdviser = External(
      id = NonEmptyString.create("Teacher 001").get,
      name = NonEmptyString.create("Fake Teacher").get,
      availabilities = generateAvailabilities,
      roles = List[Role](generateCoAdviser())
    )

    // Act

    val jury =
      Jury.create(
        president,
        adviser,
        List[Resource](supervisor),
        List[Resource](coAdviser)
      )

    //Assert
    jury.isFailure shouldBe true
  }

  test("jury with correct elements should be constituted") {

    // Arrange
    val president = Teacher(
      id = NonEmptyString.create("Teacher 001").get,
      name = NonEmptyString.create("Fake Teacher1").get,
      availabilities = generateAvailabilities,
      roles = List[Role](generatePresident())
    )

    val adviser = Teacher(
      id = NonEmptyString.create("Teacher 002").get,
      name = NonEmptyString.create("Fake Teacher2").get,
      availabilities = generateAvailabilities,
      roles = List[Role](generateAdviser())
    )

    val supervisor = External(
      id = NonEmptyString.create("Teacher 003").get,
      name = NonEmptyString.create("Fake Teacher").get,
      availabilities = generateAvailabilities,
      roles = List[Role](generateSupervisor())
    )

    val coAdviser = External(
      id = NonEmptyString.create("Teacher 004").get,
      name = NonEmptyString.create("Fake Teacher").get,
      availabilities = generateAvailabilities,
      roles = List[Role](generateCoAdviser())
    )

    // Act

    val jury =
      Jury.create(
        president,
        adviser,
        List[Resource](supervisor),
        List[Resource](coAdviser)
      )

    //Assert
    jury.isSuccess shouldBe true
  }

  test(
    "hash code should be the sum of president and adviser and supervisors and coadvisers"
  ) {

    // Arrange
    val president = Teacher(
      id = NonEmptyString.create("Teacher 001").get,
      name = NonEmptyString.create("Fake Teacher").get,
      availabilities = generateAvailabilities,
      roles = List[Role](generatePresident())
    )

    val adviser = Teacher(
      id = NonEmptyString.create("Teacher 002").get,
      name = NonEmptyString.create("Fake Teacher").get,
      availabilities = generateAvailabilities,
      roles = List[Role](generateAdviser())
    )

    val supervisor = External(
      id = NonEmptyString.create("Teacher 003").get,
      name = NonEmptyString.create("Fake Teacher").get,
      availabilities = generateAvailabilities,
      roles = List[Role](generateSupervisor())
    )

    val coAdviser = External(
      id = NonEmptyString.create("Teacher 004").get,
      name = NonEmptyString.create("Fake Teacher").get,
      availabilities = generateAvailabilities,
      roles = List[Role](generateCoAdviser())
    )

    val expectedHasCode = president.hashCode() + adviser
      .hashCode() + List[Resource](supervisor).hashCode() + List[Resource](
      coAdviser
    ).hashCode()

    // Act

    val juryHashCode =
      Jury
        .create(
          president,
          adviser,
          List[Resource](supervisor),
          List[Resource](coAdviser)
        )
        .get
        .hashCode()

    //Assert
    juryHashCode shouldEqual expectedHasCode
  }

  test(
    "equality should be given by objects hash code, so a jury instance X with different hash code than jury Y should not be equal to instance Y"
  ) {

    // Arrange
    val presidentX = Teacher(
      id = NonEmptyString.create("Teacher X1").get,
      name = NonEmptyString.create("Fake Teacher X").get,
      availabilities = generateAvailabilities,
      roles = List[Role](generatePresident())
    )

    val adviserX = Teacher(
      id = NonEmptyString.create("Teacher X2").get,
      name = NonEmptyString.create("Fake Teacher X").get,
      availabilities = generateAvailabilities,
      roles = List[Role](generateAdviser())
    )

    val presidentY = Teacher(
      id = NonEmptyString.create("Teacher 001").get,
      name = NonEmptyString.create("Fake Teacher").get,
      availabilities = generateAvailabilities,
      roles = List[Role](generatePresident())
    )

    val adviserY = Teacher(
      id = NonEmptyString.create("Teacher Y").get,
      name = NonEmptyString.create("Fake Teacher Y").get,
      availabilities = generateAvailabilities,
      roles = List[Role](generateAdviser())
    )

    // Act

    val juryX =
      Jury.create(presidentX, adviserX, List[Resource](), List[Resource]())

    val juryY =
      Jury.create(presidentY, adviserY, List[Resource](), List[Resource]())

    val juryXHashCode = juryX.get.hashCode()
    val juryYHashCode = juryY.get.hashCode()

    val equality = juryX.equals(juryY)

    //Assert

    juryXHashCode shouldNot equal(juryYHashCode)

    equality shouldBe false
  }

  test("jury with multiple elements, count should be equal to initial elements") {

    // Arrange
    val president = Teacher(
      id = NonEmptyString.create("Teacher 001").get,
      name = NonEmptyString.create("Fake Teacher").get,
      availabilities = generateAvailabilities,
      roles = List[Role](generatePresident())
    )

    val adviser = Teacher(
      id = NonEmptyString.create("Teacher 002").get,
      name = NonEmptyString.create("Fake Teacher").get,
      availabilities = generateAvailabilities,
      roles = List[Role](generateAdviser())
    )

    val supervisor = External(
      id = NonEmptyString.create("External 001").get,
      name = NonEmptyString.create("Fake External").get,
      availabilities = generateAvailabilities,
      roles = List[Role](generateSupervisor())
    )

    val coAdviser = External(
      id = NonEmptyString.create("External 002").get,
      name = NonEmptyString.create("External Teacher").get,
      availabilities = generateAvailabilities,
      roles = List[Role](generateCoAdviser())
    )

    // Act

    val jury =
      Jury.create(
        president,
        adviser,
        List[Resource](supervisor),
        List[Resource](coAdviser)
      )

    val jurySet = jury.get.asResourcesSet
    //Assert

    jurySet.size shouldBe 4
  }

  def generateAvailability(pref: Int): Availability = {

    val start = LocalDateTime.now()
    val end = start.plusMinutes(5)
    val period = Period.create(start, end).get
    val preference = Preference.create(pref).get

    Availability.create(period, preference)
  }

  def generateAvailabilities: List[Availability] = {

    List[Availability](
      generateAvailability(1),
      generateAvailability(2),
      generateAvailability(3)
    )
  }

  def generatePresident(): President = {
    President()
  }

  def generateAdviser(): Adviser = {
    Adviser()
  }

  def generateCoAdviser(): CoAdviser = {
    CoAdviser()
  }

  def generateSupervisor(): Supervisor = {
    Supervisor()
  }
}
