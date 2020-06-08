package domain.model

import java.time.LocalDateTime

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class VivasServiceTest extends AnyFunSuite with Matchers {

  test(
    "empty vivas list returns empty division of vivas difference and intersection"
  ) {

    // Arrange

    val vivas = List[Viva]()

    // Act

    val divisionVivasDifferenceAndIntersection =
      VivasService.differAndIntersect(vivas)

    // Assert

    val expectedDivision = (Set(), Set())

    divisionVivasDifferenceAndIntersection == expectedDivision shouldBe true

  }

  test(
    "if all vivas share at least one resource, then intersects set is the same as the input vivas list"
  ) {

    // Arrange

    val resourcesAvailabilityPeriod = Period
      .create(LocalDateTime.now(), LocalDateTime.now().plusMinutes(10))
      .get

    val resourcesAvailabilityPreference = Preference.create(5).get

    val sharedPresident = Teacher
      .create(
        NonEmptyString.create("T001").get,
        NonEmptyString.create("Most wanted president").get,
        List(
          Availability
            .create(
              resourcesAvailabilityPeriod,
              resourcesAvailabilityPreference
            )
        ),
        List(President())
      )
      .get

    val differentAdviserJuryX = Teacher
      .create(
        NonEmptyString.create("T002").get,
        NonEmptyString.create("Adviser #1").get,
        List(
          Availability
            .create(
              resourcesAvailabilityPeriod,
              resourcesAvailabilityPreference
            )
        ),
        List(Adviser())
      )
      .get

    val differentAdviserJuryY = Teacher
      .create(
        NonEmptyString.create("T003").get,
        NonEmptyString.create("Adviser #2").get,
        List(
          Availability
            .create(
              resourcesAvailabilityPeriod,
              resourcesAvailabilityPreference
            )
        ),
        List(Adviser())
      )
      .get

    val differentAdviserJuryZ = Teacher
      .create(
        NonEmptyString.create("T004").get,
        NonEmptyString.create("Adviser #3").get,
        List(
          Availability
            .create(
              resourcesAvailabilityPeriod,
              resourcesAvailabilityPreference
            )
        ),
        List(Adviser())
      )
      .get

    val differentAdviserJuryW = Teacher
      .create(
        NonEmptyString.create("T005").get,
        NonEmptyString.create("Adviser #4").get,
        List(
          Availability
            .create(
              resourcesAvailabilityPeriod,
              resourcesAvailabilityPreference
            )
        ),
        List(Adviser())
      )
      .get

    val juryX =
      Jury.create(sharedPresident, differentAdviserJuryX, List(), List()).get

    val juryY =
      Jury.create(sharedPresident, differentAdviserJuryY, List(), List()).get

    val juryZ =
      Jury.create(sharedPresident, differentAdviserJuryZ, List(), List()).get

    val juryW =
      Jury.create(sharedPresident, differentAdviserJuryW, List(), List()).get

    val vivasDuration = Duration.create(java.time.Duration.ofMinutes(5)).get

    val vivaX = Viva.create(
      NonEmptyString.create("Student #1").get,
      NonEmptyString.create("Viva X").get,
      juryX,
      vivasDuration
    )

    val vivaY = Viva.create(
      NonEmptyString.create("Student #2").get,
      NonEmptyString.create("Viva Y").get,
      juryY,
      vivasDuration
    )

    val vivaZ = Viva.create(
      NonEmptyString.create("Student #3").get,
      NonEmptyString.create("Viva Z").get,
      juryZ,
      vivasDuration
    )

    val vivaW = Viva.create(
      NonEmptyString.create("Student #4").get,
      NonEmptyString.create("Viva W").get,
      juryW,
      vivasDuration
    )

    val vivas = List[Viva](vivaX, vivaY, vivaZ, vivaW)

    // Act

    val divisionVivasDifferenceAndIntersection =
      VivasService.differAndIntersect(vivas)

    // Assert

    val expectedDivision = (Set(), Set(vivaX, vivaY, vivaZ, vivaW))

    divisionVivasDifferenceAndIntersection == expectedDivision shouldBe true

  }

  test(
    "if no vivas share at least one resource, then difference set is the same as the input vivas list"
  ) {

    // Arrange

    val resourcesAvailabilityPeriod = Period
      .create(LocalDateTime.now(), LocalDateTime.now().plusMinutes(10))
      .get

    val resourcesAvailabilityPreference = Preference.create(5).get

    val differentPresidentJuryX = Teacher
      .create(
        NonEmptyString.create("T001").get,
        NonEmptyString.create("President #1").get,
        List(
          Availability
            .create(
              resourcesAvailabilityPeriod,
              resourcesAvailabilityPreference
            )
        ),
        List(President())
      )
      .get

    val differentPresidentJuryY = Teacher
      .create(
        NonEmptyString.create("T002").get,
        NonEmptyString.create("President #2").get,
        List(
          Availability
            .create(
              resourcesAvailabilityPeriod,
              resourcesAvailabilityPreference
            )
        ),
        List(President())
      )
      .get

    val differentPresidentJuryZ = Teacher
      .create(
        NonEmptyString.create("T003").get,
        NonEmptyString.create("President #3").get,
        List(
          Availability
            .create(
              resourcesAvailabilityPeriod,
              resourcesAvailabilityPreference
            )
        ),
        List(President())
      )
      .get

    val differentPresidentJuryW = Teacher
      .create(
        NonEmptyString.create("T004").get,
        NonEmptyString.create("President #4").get,
        List(
          Availability
            .create(
              resourcesAvailabilityPeriod,
              resourcesAvailabilityPreference
            )
        ),
        List(President())
      )
      .get

    val differentAdviserJuryX = Teacher
      .create(
        NonEmptyString.create("T005").get,
        NonEmptyString.create("Adviser #1").get,
        List(
          Availability
            .create(
              resourcesAvailabilityPeriod,
              resourcesAvailabilityPreference
            )
        ),
        List(Adviser())
      )
      .get

    val differentAdviserJuryY = Teacher
      .create(
        NonEmptyString.create("T006").get,
        NonEmptyString.create("Adviser #2").get,
        List(
          Availability
            .create(
              resourcesAvailabilityPeriod,
              resourcesAvailabilityPreference
            )
        ),
        List(Adviser())
      )
      .get

    val differentAdviserJuryZ = Teacher
      .create(
        NonEmptyString.create("T007").get,
        NonEmptyString.create("Adviser #3").get,
        List(
          Availability
            .create(
              resourcesAvailabilityPeriod,
              resourcesAvailabilityPreference
            )
        ),
        List(Adviser())
      )
      .get

    val differentAdviserJuryW = Teacher
      .create(
        NonEmptyString.create("T008").get,
        NonEmptyString.create("Adviser #4").get,
        List(
          Availability
            .create(
              resourcesAvailabilityPeriod,
              resourcesAvailabilityPreference
            )
        ),
        List(Adviser())
      )
      .get

    val juryX =
      Jury
        .create(differentPresidentJuryX, differentAdviserJuryX, List(), List())
        .get

    val juryY =
      Jury
        .create(differentPresidentJuryY, differentAdviserJuryY, List(), List())
        .get

    val juryZ =
      Jury
        .create(differentPresidentJuryZ, differentAdviserJuryZ, List(), List())
        .get

    val juryW =
      Jury
        .create(differentPresidentJuryW, differentAdviserJuryW, List(), List())
        .get

    val vivasDuration = Duration.create(java.time.Duration.ofMinutes(5)).get

    val vivaX = Viva.create(
      NonEmptyString.create("Student #1").get,
      NonEmptyString.create("Viva X").get,
      juryX,
      vivasDuration
    )

    val vivaY = Viva.create(
      NonEmptyString.create("Student #2").get,
      NonEmptyString.create("Viva Y").get,
      juryY,
      vivasDuration
    )

    val vivaZ = Viva.create(
      NonEmptyString.create("Student #3").get,
      NonEmptyString.create("Viva Z").get,
      juryZ,
      vivasDuration
    )

    val vivaW = Viva.create(
      NonEmptyString.create("Student #4").get,
      NonEmptyString.create("Viva W").get,
      juryW,
      vivasDuration
    )

    val vivas = List[Viva](vivaX, vivaY, vivaZ, vivaW)

    // Act

    val divisionVivasDifferenceAndIntersection =
      VivasService.differAndIntersect(vivas)

    // Assert

    val expectedDivision = (Set(vivaX, vivaY, vivaZ, vivaW), Set())

    divisionVivasDifferenceAndIntersection == expectedDivision shouldBe true

  }

  test(
    "if in X vivas Y vivas share at least one resource, then the difference set is the difference of X - Y and the intersection Y"
  ) {

    // Arrange

    val resourcesAvailabilityPeriod = Period
      .create(LocalDateTime.now(), LocalDateTime.now().plusMinutes(10))
      .get

    val resourcesAvailabilityPreference = Preference.create(5).get

    val sharedPresidentJuryX = Teacher
      .create(
        NonEmptyString.create("T001").get,
        NonEmptyString.create("President #1").get,
        List(
          Availability
            .create(
              resourcesAvailabilityPeriod,
              resourcesAvailabilityPreference
            )
        ),
        List(President())
      )
      .get

    val sharedPresidentJuryY = Teacher
      .create(
        NonEmptyString.create("T002").get,
        NonEmptyString.create("President #2").get,
        List(
          Availability
            .create(
              resourcesAvailabilityPeriod,
              resourcesAvailabilityPreference
            )
        ),
        List(President())
      )
      .get

    val differentAdviserJuryX = Teacher
      .create(
        NonEmptyString.create("T005").get,
        NonEmptyString.create("Adviser #1").get,
        List(
          Availability
            .create(
              resourcesAvailabilityPeriod,
              resourcesAvailabilityPreference
            )
        ),
        List(Adviser())
      )
      .get

    val differentAdviserJuryY = Teacher
      .create(
        NonEmptyString.create("T006").get,
        NonEmptyString.create("Adviser #2").get,
        List(
          Availability
            .create(
              resourcesAvailabilityPeriod,
              resourcesAvailabilityPreference
            )
        ),
        List(Adviser())
      )
      .get

    val differentAdviserJuryZ = Teacher
      .create(
        NonEmptyString.create("T007").get,
        NonEmptyString.create("Adviser #3").get,
        List(
          Availability
            .create(
              resourcesAvailabilityPeriod,
              resourcesAvailabilityPreference
            )
        ),
        List(Adviser())
      )
      .get

    val differentAdviserJuryW = Teacher
      .create(
        NonEmptyString.create("T008").get,
        NonEmptyString.create("Adviser #4").get,
        List(
          Availability
            .create(
              resourcesAvailabilityPeriod,
              resourcesAvailabilityPreference
            )
        ),
        List(Adviser())
      )
      .get

    val juryX =
      Jury
        .create(sharedPresidentJuryX, differentAdviserJuryX, List(), List())
        .get

    val juryY =
      Jury
        .create(sharedPresidentJuryY, differentAdviserJuryY, List(), List())
        .get

    val juryZ =
      Jury
        .create(sharedPresidentJuryX, differentAdviserJuryZ, List(), List())
        .get

    val juryW =
      Jury
        .create(sharedPresidentJuryX, differentAdviserJuryW, List(), List())
        .get

    val vivasDuration = Duration.create(java.time.Duration.ofMinutes(5)).get

    val vivaX = Viva.create(
      NonEmptyString.create("Student #1").get,
      NonEmptyString.create("Viva X").get,
      juryX,
      vivasDuration
    )

    val vivaY = Viva.create(
      NonEmptyString.create("Student #2").get,
      NonEmptyString.create("Viva Y").get,
      juryY,
      vivasDuration
    )

    val vivaZ = Viva.create(
      NonEmptyString.create("Student #3").get,
      NonEmptyString.create("Viva Z").get,
      juryZ,
      vivasDuration
    )

    val vivaW = Viva.create(
      NonEmptyString.create("Student #4").get,
      NonEmptyString.create("Viva W").get,
      juryW,
      vivasDuration
    )

    val vivas = List[Viva](vivaX, vivaY, vivaZ, vivaW)

    // Act

    val divisionVivasDifferenceAndIntersection =
      VivasService.differAndIntersect(vivas)

    // Assert

    val expectedDivision = (Set(vivaY), Set(vivaX, vivaZ, vivaW))

    divisionVivasDifferenceAndIntersection == expectedDivision shouldBe true

  }
}
