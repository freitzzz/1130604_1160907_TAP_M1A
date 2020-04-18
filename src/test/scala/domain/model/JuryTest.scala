package domain.model

import java.time.LocalDateTime

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class JuryTest extends AnyFunSuite with Matchers {

  test("jury should not accept to be constituted without president") {

    // Arrange
    val president = Teacher(
      id = "Teacher 001",
      name = "Fake Teacher",
      availabilities = generateAvailabilities,
      roles = List[Role](generateAdviser())
    )

    val adviser = Teacher(
      id = "Teacher 001",
      name = "Fake Teacher",
      availabilities = generateAvailabilities,
      roles = List[Role](generateAdviser())
    )

    // Act

    val jury =
      Jury.create(president, adviser, List[Resource](), List[Resource]())

    //Assert

    jury shouldBe None
  }

  test("jury should not accept to be constituted without adviser") {

    // Arrange
    val president = Teacher(
      id = "Teacher 001",
      name = "Fake Teacher",
      availabilities = generateAvailabilities,
      roles = List[Role](generatePresident())
    )

    val adviser = Teacher(
      id = "Teacher 001",
      name = "Fake Teacher",
      availabilities = generateAvailabilities,
      roles = List[Role](generatePresident())
    )

    // Act

    val jury =
      Jury.create(president, adviser, List[Resource](), List[Resource]())

    //Assert

    jury shouldBe None
  }

  test("jury with supervisors should all be supervisors") {

    // Arrange
    val president = Teacher(
      id = "Teacher 001",
      name = "Fake Teacher",
      availabilities = generateAvailabilities,
      roles = List[Role](generatePresident())
    )

    val adviser = Teacher(
      id = "Teacher 001",
      name = "Fake Teacher",
      availabilities = generateAvailabilities,
      roles = List[Role](generateAdviser())
    )

    val supervisor = External(
      id = "Teacher 001",
      name = "Fake Teacher",
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

    jury shouldBe None
  }

  test("jury with coadvisers should all be coadvisers") {

    // Arrange
    val president = Teacher(
      id = "Teacher 001",
      name = "Fake Teacher",
      availabilities = generateAvailabilities,
      roles = List[Role](generatePresident())
    )

    val adviser = Teacher(
      id = "Teacher 001",
      name = "Fake Teacher",
      availabilities = generateAvailabilities,
      roles = List[Role](generateAdviser())
    )

    val coAdviser = External(
      id = "Teacher 001",
      name = "Fake Teacher",
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

    jury shouldBe None
  }

  test("jury with correct elements should be constituted") {

    // Arrange
    val president = Teacher(
      id = "Teacher 001",
      name = "Fake Teacher",
      availabilities = generateAvailabilities,
      roles = List[Role](generatePresident())
    )

    val adviser = Teacher(
      id = "Teacher 001",
      name = "Fake Teacher",
      availabilities = generateAvailabilities,
      roles = List[Role](generateAdviser())
    )

    val supervisor = External(
      id = "Teacher 001",
      name = "Fake Teacher",
      availabilities = generateAvailabilities,
      roles = List[Role](generateSupervisor())
    )

    val coAdviser = External(
      id = "Teacher 001",
      name = "Fake Teacher",
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
    jury shouldBe Some(jury.get)
  }

  test(
    "hash code should be the sum of president and adviser and supervisors and coadvisers"
  ) {

    // Arrange
    val president = Teacher(
      id = "Teacher 001",
      name = "Fake Teacher",
      availabilities = generateAvailabilities,
      roles = List[Role](generatePresident())
    )

    val adviser = Teacher(
      id = "Teacher 001",
      name = "Fake Teacher",
      availabilities = generateAvailabilities,
      roles = List[Role](generateAdviser())
    )

    val supervisor = External(
      id = "Teacher 001",
      name = "Fake Teacher",
      availabilities = generateAvailabilities,
      roles = List[Role](generateSupervisor())
    )

    val coAdviser = External(
      id = "Teacher 001",
      name = "Fake Teacher",
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
      id = "Teacher X",
      name = "Fake Teacher X",
      availabilities = generateAvailabilities,
      roles = List[Role](generatePresident())
    )

    val adviserX = Teacher(
      id = "Teacher X",
      name = "Fake Teacher X",
      availabilities = generateAvailabilities,
      roles = List[Role](generateAdviser())
    )

    val presidentY = Teacher(
      id = "Teacher 001",
      name = "Fake Teacher",
      availabilities = generateAvailabilities,
      roles = List[Role](generatePresident())
    )

    val adviserY = Teacher(
      id = "Teacher Y",
      name = "Fake Teacher Y",
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

  def generateAvailability(pref: Int): Availability = {

    val start = LocalDateTime.now()
    val end = start.plusMinutes(5)
    val preference = pref

    Availability.create(start, end, preference).orNull
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
