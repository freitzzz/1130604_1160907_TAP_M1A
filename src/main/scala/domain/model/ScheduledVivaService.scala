package domain.model

import scala.util.{Failure, Try}

object ScheduledVivaService {

  // O(N^3) = O(N^2) + O(N^3)
  def findPeriodsInWhichVivaCanOccur(viva: Viva): Set[Period] = {

    val vivaDuration = viva.duration

    val vivaResources = viva.jury.asResourcesSet

    val resourcesAvailabilitiesPeriodsPossibleForVivaDuration =
      vivaResources
        .flatMap(_.availabilitiesPossibleFor(vivaDuration).map(_.period))

    resourcesAvailabilitiesPeriodsPossibleForVivaDuration.filter(
      period =>
        vivaResources.forall(
          _.isAvailableOn(
            Period
              .create(
                period.start,
                period.start.plus(vivaDuration.timeDuration)
              )
              .get
          )
      )
    )

  }

  def calculateSumOfPreferences(vivaJuryResources: Set[Resource],
                                vivaPeriod: Period): Int = {
    val sumOfPreferences = vivaJuryResources.toList
      .flatMap(resource => resource.availabilityOn(vivaPeriod))
      .foldLeft(0)(_ + _.preference.value)

    sumOfPreferences
  }

  def updateVivasAccordingToScheduledVivaPeriod(scheduledVivaJury: Jury,
                                                vivasToUpdate: List[Viva],
                                                period: Period): List[Viva] = {

    val updatedPresident =
      newResource(scheduledVivaJury.president, period)

    val updatedAdviser =
      newResource(scheduledVivaJury.adviser, period)

    val updatedSupervisors =
      scheduledVivaJury.supervisors
        .map(supervisor => newResource(supervisor, period))

    val updatedCoAdvisers =
      scheduledVivaJury.coAdvisers
        .map(coAdviser => newResource(coAdviser, period))

    val allUpdatedResources =
      updatedSupervisors ++ updatedCoAdvisers ++ List(updatedPresident) ++ List(
        updatedAdviser
      )

    val updatedVivas =
      vivasToUpdate.map(viva => updateViva(viva, allUpdatedResources))

    updatedVivas

  }

  def ScheduleVivasIndividually(vivas: List[Viva]): List[Try[ScheduledViva]] = {

    vivas.map(
      viva =>
        findResourcesMaxedAvailability(viva) match {
          case Some(value) => ScheduledViva.create(viva, value)
          case None =>
            Failure(
              new IllegalStateException(
                s"Viva $viva could not be scheduled as at least one resource is not available in the viva period"
              )
            )
      }
    )
  }

  /*
  In this method we go through the availabilities of the resources. We start by looking for the maxed preference of each resource.
  The algorithm keeps looking to the next maxed availabilities until it finds a period of time where all resources are
  simultaneously available.
   */
  private def findResourcesMaxedAvailability(viva: Viva): Option[Period] = {

    val resourcesSet = viva.jury.asResourcesSet

    val maxedPeriods = resourcesSet.toList
      .flatMap(resource => resource.availabilities)
      .map(
        availability =>
          Period
            .create(
              availability.period.start,
              availability.period.start.plus(viva.duration.timeDuration)
            )
            .get
      )

    val periodsWhichResourcesAreAvailable = maxedPeriods.filter(
      period => resourcesSet.forall(_.isAvailableOn(period))
    )

    val maximizedPeriodsWhichResourcesAvailablePerScheduledPreference =
      periodsWhichResourcesAreAvailable
        .map(
          period =>
            (
              period,
              ScheduledVivaService
                .calculateSumOfPreferences(resourcesSet, period)
          )
        )
        .sortBy(-_._2)

    maximizedPeriodsWhichResourcesAvailablePerScheduledPreference match {
      case ::(head, _) =>
        if (maximizedPeriodsWhichResourcesAvailablePerScheduledPreference.count(
              _._2 == head._2
            ) == 1) {
          Some(head._1)
        } else {
          Some(
            maximizedPeriodsWhichResourcesAvailablePerScheduledPreference
              .filter(_._2 == head._2)
              .sortBy(_._1.start)
              .headOption
              .get
              ._1
          )
        }
      case Nil => None
    }
  }

  private def updateViva(viva: Viva, updatedResources: List[Resource]) = {

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

    Viva
      .create(viva.student, viva.title, updatedJury, viva.duration)

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
      .map(period => Availability.create(period.get, availability.preference))
  }
}
