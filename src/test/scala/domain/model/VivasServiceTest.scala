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

    val expectedDivision =
      ((Set[Viva](), List[Resource]()), (Set[Viva](), List[Resource]()))

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

    val expectedDivision = (
      (
        Set[Viva](),
        List[Resource](
          differentAdviserJuryX,
          differentAdviserJuryY,
          differentAdviserJuryZ,
          differentAdviserJuryW
        )
      ),
      (Set[Viva](vivaX, vivaY, vivaZ, vivaW), List[Resource](sharedPresident))
    )

    val expectedDivisionVivas =
      (expectedDivision._1._1, expectedDivision._2._1)

    val actualDivisionVivas = (
      divisionVivasDifferenceAndIntersection._1._1,
      divisionVivasDifferenceAndIntersection._2._1
    )

    actualDivisionVivas == expectedDivisionVivas shouldBe true

    divisionVivasDifferenceAndIntersection._1._2.size == expectedDivision._1._2.size shouldBe true

    divisionVivasDifferenceAndIntersection._2._2.size == expectedDivision._2._2.size shouldBe true

    divisionVivasDifferenceAndIntersection._2._2
      .forall(expectedDivision._2._2.contains) shouldBe true

    divisionVivasDifferenceAndIntersection._1._2
      .forall(expectedDivision._1._2.contains) shouldBe true

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

    val expectedDivision =
      (
        (
          Set[Viva](vivaX, vivaY, vivaZ, vivaW),
          List[Resource](
            differentPresidentJuryX,
            differentPresidentJuryY,
            differentPresidentJuryZ,
            differentPresidentJuryW,
            differentAdviserJuryX,
            differentAdviserJuryY,
            differentAdviserJuryW,
            differentAdviserJuryZ
          )
        ),
        (Set[Viva](), List[Resource]())
      )

    val expectedDivisionVivas =
      (expectedDivision._1._1, expectedDivision._2._1)

    val actualDivisionVivas = (
      divisionVivasDifferenceAndIntersection._1._1,
      divisionVivasDifferenceAndIntersection._2._1
    )

    actualDivisionVivas == expectedDivisionVivas shouldBe true

    divisionVivasDifferenceAndIntersection._1._2.size == expectedDivision._1._2.size shouldBe true

    divisionVivasDifferenceAndIntersection._2._2.size == expectedDivision._2._2.size shouldBe true

    divisionVivasDifferenceAndIntersection._2._2
      .forall(expectedDivision._2._2.contains) shouldBe true

    divisionVivasDifferenceAndIntersection._1._2
      .forall(expectedDivision._1._2.contains) shouldBe true

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

    val expectedDivision =
      (
        (
          Set[Viva](vivaY),
          List[Resource](
            sharedPresidentJuryY,
            differentAdviserJuryX,
            differentAdviserJuryY,
            differentAdviserJuryZ,
            differentAdviserJuryW,
          )
        ),
        (Set[Viva](vivaX, vivaZ, vivaW), List[Resource](sharedPresidentJuryX))
      )

    val expectedDivisionVivas =
      (expectedDivision._1._1, expectedDivision._2._1)

    val actualDivisionVivas = (
      divisionVivasDifferenceAndIntersection._1._1,
      divisionVivasDifferenceAndIntersection._2._1
    )

    actualDivisionVivas == expectedDivisionVivas shouldBe true

    divisionVivasDifferenceAndIntersection._1._2.size == expectedDivision._1._2.size shouldBe true

    divisionVivasDifferenceAndIntersection._2._2.size == expectedDivision._2._2.size shouldBe true

    divisionVivasDifferenceAndIntersection._2._2
      .forall(expectedDivision._2._2.contains) shouldBe true

    divisionVivasDifferenceAndIntersection._1._2
      .forall(expectedDivision._1._2.contains) shouldBe true

  }

  test(
    "given a list of vivas where there are vivas that share the same exact resources, then a set with those vivas should be returned and should never be empty when calling findVivasThatShareTheSameJury"
  ) {
    val resourcesAvailabilityPeriod = Period
      .create(LocalDateTime.now(), LocalDateTime.now().plusMinutes(10))
      .get

    val resourcesAvailabilityPreference = Preference.create(5).get

    val sharedPresidentBetweenViva1AndViva2 = Teacher
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

    val sharedAdviserBetweenViva1AndViva2 = Teacher
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

    val juryViva1 =
      Jury
        .create(
          sharedPresidentBetweenViva1AndViva2,
          sharedAdviserBetweenViva1AndViva2,
          List(),
          List()
        )
        .get

    val juryViva2 =
      Jury
        .create(
          sharedPresidentBetweenViva1AndViva2,
          sharedAdviserBetweenViva1AndViva2,
          List(),
          List()
        )
        .get

    val vivasDuration = Duration.create(java.time.Duration.ofMinutes(5)).get

    val viva1 = Viva.create(
      NonEmptyString.create("Student #1").get,
      NonEmptyString.create("Viva 1").get,
      juryViva1,
      vivasDuration
    )

    val viva2 = Viva.create(
      NonEmptyString.create("Student #2").get,
      NonEmptyString.create("Viva 2").get,
      juryViva2,
      vivasDuration
    )

    val vivas = List[Viva](viva1, viva2)

    //Act

    val vivasThatShareTheSameResources =
      VivasService.findVivasThatShareTheSameJury(vivas)

    //Assert

    vivasThatShareTheSameResources shouldBe Map[Set[Resource], List[Viva]](
      (
        Set[Resource](
          sharedPresidentBetweenViva1AndViva2,
          sharedAdviserBetweenViva1AndViva2
        ),
        List[Viva](viva1, viva2)
      )
    )

  }

  test(
    "given a list of vivas that never share the same exact resources, then when calling findVivasThatShareTheSameJury an empty set should be returned"
  ) {

    val resourcesAvailabilityPeriod = Period
      .create(LocalDateTime.now(), LocalDateTime.now().plusMinutes(10))
      .get

    val resourcesAvailabilityPreference = Preference.create(5).get

    val sharedPresidentBetweenViva1AndViva2 = Teacher
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

    val adviserExclusiveToViva1 = Teacher
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

    val adviserExclusiveToViva2 = Teacher
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

    val juryViva1 =
      Jury
        .create(
          sharedPresidentBetweenViva1AndViva2,
          adviserExclusiveToViva1,
          List(),
          List()
        )
        .get

    val juryViva2 =
      Jury
        .create(
          sharedPresidentBetweenViva1AndViva2,
          adviserExclusiveToViva2,
          List(),
          List()
        )
        .get

    val vivasDuration = Duration.create(java.time.Duration.ofMinutes(5)).get

    val viva1 = Viva.create(
      NonEmptyString.create("Student #1").get,
      NonEmptyString.create("Viva 1").get,
      juryViva1,
      vivasDuration
    )

    val viva2 = Viva.create(
      NonEmptyString.create("Student #2").get,
      NonEmptyString.create("Viva 2").get,
      juryViva2,
      vivasDuration
    )

    val vivas = List[Viva](viva1, viva2)

    //Act

    val vivasThatShareTheSameResources =
      VivasService.findVivasThatShareTheSameJury(vivas)

    //Assert

    vivasThatShareTheSameResources.size shouldBe 0
  }

  test(
    "given an empty list, the result of findVivasThatShareTheSameJury should be an empty map"
  ) {

    // Arrange

    val vivas = List[Viva]()

    // Act

    val vivasThatShareTheSameJury =
      VivasService.findVivasThatShareTheSameJury(vivas)

    // Assert

    vivasThatShareTheSameJury shouldBe Map.empty[Set[Resource], List[Viva]]
  }
}
