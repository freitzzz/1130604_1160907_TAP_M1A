package diffScheduler

import java.time.{LocalDate, LocalDateTime}

import domain.model.{Adviser, Availability, Duration, External, Jury, NonEmptyString, Period, Preference, President, Resource, Role, Supervisor, Teacher, Viva}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class DiffSchedulerTest extends AnyFunSuite with Matchers {

  test("given a list of vivas, the maxed availability is found"){

    // Arrange
    val availability1T001 = Availability.create(
      Period.create(
        LocalDateTime.of(2020, 5, 30, 9, 30, 0),
        LocalDateTime.of(2020, 5, 30, 9, 30, 0).plusHours(3)).get,
      Preference.create(5).get)

    val availability2T001 = Availability.create(
      Period.create(
        LocalDateTime.of(2020, 5, 30, 13, 30, 0),
        LocalDateTime.of(2020, 5, 30, 13, 30, 0).plusHours(3)).get,
      Preference.create(3).get)

    val availability1T002 = Availability.create(
      Period.create(
        LocalDateTime.of(2020, 5, 30, 10, 30, 0),
        LocalDateTime.of(2020, 5, 30, 10, 30, 0).plusHours(1)).get,
      Preference.create(5).get)

    val availability2T002 = Availability.create(
      Period.create(
        LocalDateTime.of(2020, 5, 30, 14, 30, 0),
        LocalDateTime.of(2020, 5, 30, 14, 30, 0).plusMinutes(90)).get,
      Preference.create(5).get)

    val availability1E001 = Availability.create(
      Period.create(
        LocalDateTime.of(2020, 5, 30, 10, 0, 0),
        LocalDateTime.of(2020, 5, 30, 10, 0, 0).plusMinutes(210)).get,
      Preference.create(2).get)


    val availability2E001 = Availability.create(
      Period.create(
        LocalDateTime.of(2020, 5, 30, 15, 30, 0),
        LocalDateTime.of(2020, 5, 30, 15, 30, 0).plusMinutes(150)).get,
      Preference.create(5).get)

    val availability1E002 = Availability.create(
      Period.create(
        LocalDateTime.of(2020, 5, 30, 10, 0, 0),
        LocalDateTime.of(2020, 5, 30, 10, 0, 0).plusMinutes(210)).get,
      Preference.create(5).get)

    val teacher001 = Teacher.create(
      NonEmptyString.create("T001").get,
      NonEmptyString.create("Teacher 001").get,
      List[Availability](availability1T001, availability2T001),
      List[Role](President(), Adviser()))

    val teacher002 = Teacher.create(
      NonEmptyString.create("T002").get,
      NonEmptyString.create("Teacher 002").get,
      List[Availability](availability1T002, availability2T002),
      List[Role](Adviser(), President()))

    val external001 = External.create(
      NonEmptyString.create("E001").get,
      NonEmptyString.create("External 001").get,
      List[Availability](availability1E001, availability2E001),
      List[Role](Supervisor()))

    val external002 = External.create(
      NonEmptyString.create("E002").get,
      NonEmptyString.create("External 002").get,
      List[Availability](availability1E002),
      List[Role]())

    val vivas = List[Viva](
      Viva.create(
        NonEmptyString.create("Student 001").get,
        NonEmptyString.create("Title 1").get,
        Jury.create(
          president = teacher001.get,
          adviser = teacher002.get,
          supervisors = List[Resource](external001.get),
          coAdvisers = List[Resource]()).get,
        duration = Duration.create(java.time.Duration.ofMinutes(60)).get),
      Viva.create(
        NonEmptyString.create("Student 002").get,
        NonEmptyString.create("Title 2").get,
        Jury.create(
          president = teacher002.get,
          adviser = teacher001.get,
          supervisors = List[Resource](external001.get),
          coAdvisers = List[Resource]()).get,
        duration = Duration.create(java.time.Duration.ofMinutes(60)).get)
    )
    // Act
    val scheduledVivas = DiffScheduler.DiffScheduler.ScheduleVivasIndividually(vivas)

    val preferencesFromScheduledVivas = scheduledVivas.map(x => x.get.scheduledPreference).sum

    // Assert
    preferencesFromScheduledVivas shouldBe 25
  }
}
