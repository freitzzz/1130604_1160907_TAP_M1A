package assessment

import java.time.Duration

import domain.model.{
  Agenda,
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
import xml.Functions

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}
import scala.xml.Elem

object AssessmentMS01 extends Schedule {
  // TODO: Use the functions in your own code to implement the assessment of ms01
  def create(xml: Elem): Try[Elem] = {
    val vivasParse = Functions.deserialize(xml)

    vivasParse match {
      case Failure(exception) => Failure(exception)
      case Success(value) => {

        val vivas = value

        val scheduledVivas = scheduleVivas(vivas)

        scheduledVivas.find(_.isFailure) match {
          //case Some(value) => Failure(value.failed.get)
          case Some(value) => Success(Functions.serializeError(value.failed.get))
          case None => {
            Success(Functions.serialize(Agenda(scheduledVivas.map(_.get))))
          }
        }
      }
    }
  }

  private def scheduleVivas(vivas: List[Viva]): List[Try[ScheduledViva]] = {

    @tailrec
    def auxScheduleVivas(
      vivas: List[Viva],
      scheduledVivas: List[Try[ScheduledViva]]
    ): List[Try[ScheduledViva]] = {

      vivas match {
        case ::(head, next) =>
          val periodOption = findFirstPeriodWhichAllResourcesAreAvailable(head)

          periodOption match {
            case Some(value) =>
              val updatedVivas = updateVivas(head.jury, next, value)

              val scheduledViva = ScheduledViva
                .create(head, value)

              auxScheduleVivas(updatedVivas, scheduledViva :: scheduledVivas)
            case None =>
              List[Try[ScheduledViva]](
                Failure(
                  new IllegalStateException(
                    "Not all Jury elements share a compatible availability"
                  )
                )
              )
          }
        case Nil => scheduledVivas.reverse
      }

    }

    auxScheduleVivas(vivas, List[Try[ScheduledViva]]())
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

  private def updateVivas(jury: Jury,
                          tailVivas: List[Viva],
                          period: Period): List[Viva] = {

    val updatedPresident =
      newResource(jury.president, period)

    val updatedAdviser =
      newResource(jury.adviser, period)

    val updatedSupervisors =
      jury.supervisors
        .map(supervisor => newResource(supervisor, period))

    val updatedCoAdvisers =
      jury.coAdvisers
        .map(coAdviser => newResource(coAdviser, period))

    val vivaDuration = Duration.between(period.start, period.end)

    val allUpdatedResources =
      updatedSupervisors ++ updatedCoAdvisers ++ List(updatedPresident) ++ List(
        updatedAdviser
      )

    val updatedVivas =
      tailVivas.map(viva => updateViva(viva, allUpdatedResources, vivaDuration))

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
