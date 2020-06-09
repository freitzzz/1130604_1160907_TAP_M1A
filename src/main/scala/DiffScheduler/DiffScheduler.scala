package DiffScheduler

import domain.model.{ Period, ScheduledViva, Viva}

import scala.util.Try

object DiffScheduler {

  def ScheduleVivasIndividually(vivas: List[Viva]): List[Try[ScheduledViva]] = {

    vivas.map(viva => ScheduledViva.create(viva, findResourcesMaxedAvailability(viva).get))
  }

  /*
  In this method we go through the availabilities of the resources. We start by looking for the maxed preference of each resource.
  The algorithm keeps looking to the next maxed availabilities until it finds a period of time where all resources are
  simultaneously available.
   */
  def findResourcesMaxedAvailability(viva: Viva): Option[Period] = {

    val resourcesSet = viva.jury.asResourcesSet

    val maxedPeriods = resourcesSet
      .flatMap(resource => resource.availabilities)
      .toList
      .sortBy(x => x.preference.value)
      .reverse
      .map(availability => Period.create(availability.period.start, availability.period.start.plus(viva.duration.timeDuration)).get)

    maxedPeriods.find(period => resourcesSet.forall(resource => resource.isAvailableOn(period)))
  }
}
