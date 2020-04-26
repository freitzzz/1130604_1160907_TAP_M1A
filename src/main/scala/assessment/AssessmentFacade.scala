package assessment

import java.time.Duration

import domain.model.{
  Availability,
  External,
  Jury,
  Period,
  Resource,
  ScheduledViva,
  Teacher,
  Viva
}
import domain.schedule._
import xml.Parser

import scala.util.{Failure, Success, Try}
import scala.xml.Elem

object AssessmentMS01 extends Schedule {
  // TODO: Use the functions in your own code to implement the assessment of ms01
  def create(xml: Elem): Try[Elem] = {
    val vivasParse = Parser.parse(xml)

    if (vivasParse.isSuccess) {

      val vivas = vivasParse.get

      val asdsad = scheduleVivas(vivas)

      println(asdsad)

      Success(null)

    } else {
      Failure(vivasParse.failed.get)
    }
  }

  private def scheduleVivas(
    vivas: List[Viva]
  ): Try[List[Try[ScheduledViva]]] = {

    def auxScheduleVivas(vivas: List[Viva]): Try[List[Try[ScheduledViva]]] = {

      vivas match {
        case ::(head, next) => {
          val periodOption = findFirstPeriodWhichAllResourcesAreAvailable(head)

          if (periodOption.nonEmpty) {

            val period = periodOption.get

            println(period)

            // TODO: VIVA TEM QUE PASSAR A TER UMA DURACAO E  COM BASE NESTA O PERIODO EM QUE OS RESOURCES TE MQ UEESTAR DISPONIVEIS É O COMEÇO DO PERIODO DESTES + DURACAO

            val updatedVivas = updateVivas(head, next, period)

            val recCall = auxScheduleVivas(updatedVivas.tail)

            if (recCall.isSuccess)
              Success(
                ScheduledViva.create(updatedVivas.head, period) :: recCall.get
              )
            else
              Failure(recCall.failed.get)

          } else {
            Failure(
              new IllegalStateException(
                "Not all Jury elements share a compatible availability"
              )
            )
          }
        }
        case Nil => Success(Nil)
      }

    }

    auxScheduleVivas(vivas)
  }

  private def findFirstPeriodWhichAllResourcesAreAvailable(
    viva: Viva
  ): Option[Period] = {

    val ordPeriods = viva.jury.asResourcesSet
      .flatMap(resource => resource.availabilities)
      .toList
      .sortWith((a1, a2) => a1.period.start.isBefore(a2.period.start))
      .map(availability => availability.period)
      .map(
        period =>
          Period.create(period.start, period.start.plus(viva.duration)).get
      )

    ordPeriods
      .find(
        period =>
          viva.jury.asResourcesSet
            .forall(resource => resource.isAvailableOn(period))
      )

  }

  private def updateVivas(headViva: Viva,
                          tailVivas: List[Viva],
                          period: Period): List[Viva] = {

    val updatedPresident =
      newResource(headViva.jury.president, period)

    val updatedAdviser =
      newResource(headViva.jury.adviser, period)

    val updatedSupervisors =
      headViva.jury.supervisors
        .map(supervisor => newResource(supervisor, period))

    val updatedCoAdvisers =
      headViva.jury.coAdvisers
        .map(coAdviser => newResource(coAdviser, period))

    val vivaDuration = Duration.between(period.start, period.end)

    val updatedHeadViva = headViva

    val allUpdatedResources =
      updatedSupervisors ++ updatedCoAdvisers ++ List(updatedPresident) ++ List(
        updatedAdviser
      )

    val updatedVivas = updatedHeadViva :: tailVivas.map(
      viva => updateViva(viva, allUpdatedResources, vivaDuration)
    )

    updatedVivas

  }

  private def updateViva(viva: Viva,
                         updatedResources: List[Resource],
                         vivaDuration: Duration) = {

    val jury = viva.jury

    val updatedVivaJuryResources = updatedResources
      .filter(resource => jury.asResourcesSet.contains(resource))

    val newJuryVivaResources = jury.asResourcesSet.toList
      .diff(updatedVivaJuryResources) ++ updatedVivaJuryResources

    val updatedJury = Jury
      .create(
        newJuryVivaResources.find(_.id == jury.president.id).get,
        newJuryVivaResources.find(_.id == jury.adviser.id).get,
        newJuryVivaResources
          .filter(resource => jury.supervisors.map(_.id).contains(resource.id)),
        newJuryVivaResources
          .filter(resource => jury.coAdvisers.map(_.id).contains(resource.id))
      )
      .get

    Viva.create(viva.student, viva.title, updatedJury, viva.duration).get

  }

  private def newResource(resource: Resource, period: Period): Resource = {
    resource match {
      case Teacher(id, name, _, roles) =>
        Teacher
          .create(id, name, newResourceAvailabilities(resource, period), roles)
          .get
      case External(id, name, _, roles) =>
        External
          .create(id, name, newResourceAvailabilities(resource, period), roles)
          .get
    }

  }

  private def newResourceAvailabilities(resource: Resource,
                                        period: Period): List[Availability] = {

    val availabilityOnPeriod = resource.availabilityOn(period).get

    val newAvailabilities =
      splitAvailability(availabilityOnPeriod, period)

    resource.availabilities.filter(_ != availabilityOnPeriod) ++ newAvailabilities
  }

  private def splitAvailability(availability: Availability,
                                period: Period): List[Availability] = {

    List(
      Period.create(availability.period.start, period.start),
      Period.create(period.end, availability.period.end)
    ).filter(_.isSuccess)
      .map(
        period => Availability.create(period.get, availability.preference).get
      )
  }

}

object AssessmentMS03 extends Schedule {
  // TODO: Use the functions in your own code to implement the assessment of ms03
  def create(xml: Elem): Try[Elem] = ???
}

object asdasdsad {

  def asdasdasdasd = {

    /* val asda = vivas.map(
      viva =>
        viva.jury.asResourcesSet
          .flatMap(resource => resource.availabilities)
          .toList
          .sortWith((a1, a2) => a1.period.start.isBefore(a2.period.start))
          .map(availability => availability.period)
          .map(
            period =>
              (
                period,
                viva.jury.asResourcesSet
                  .forall(resource => resource.isAvailableOn(period))
              )
          )
          .find(tuple => tuple._2)
    )

    val a = LocalDateTime.parse("2020-05-30T10:30:00")

    val b = LocalDateTime.parse("2020-05-30T11:31:00")

    val c = Period.create(a, b).get

    val aa = LocalDateTime.parse("2020-05-30T10:30:00")

    val bb = LocalDateTime.parse("2020-05-30T11:30:00")

    val cc = Period.create(aa, bb).get

    val a1 = Availability.create(c, Preference.create(1).get).get

    val a2 = Availability.create(cc, Preference.create(1).get).get

    val r1 = Teacher
      .create(
        NonEmptyString.create("asd").get,
        NonEmptyString.create("asdasd").get,
        List(a2),
        List(President())
      )
      .get

    println(r1.isAvailableOn(c))

    println(asda)*/
    /* val allResourceAvailabilitiesPeriodSortedByPeriodStartDateTime =
      viva1.jury.asResourcesSet
        .flatMap(resource => resource.availabilities)
        .toList
        .sortWith((a1, a2) => a1.period.start.isBefore(a2.period.start))
        .map(availability => availability.period)

    println(allResourceAvailabilitiesPeriodSortedByPeriodStartDateTime)

    val firstPeriodWhichAllResourcesAreAvailableOn =
      allResourceAvailabilitiesPeriodSortedByPeriodStartDateTime
        .map(
          period =>
            (
              period,
              viva1.jury.asResourcesSet
                .forall(resource => resource.isAvailableOn(period))
          )
        )
        .find(tuple => tuple._2)

    println(firstPeriodWhichAllResourcesAreAvailableOn)*/
  }

}
