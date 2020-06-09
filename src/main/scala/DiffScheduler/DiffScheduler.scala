package DiffScheduler

import domain.model.{ Period, ScheduledViva, Viva}

import scala.util.Try

object DiffScheduler {

  def ScheduleVivasIndividually(vivas: List[Viva]): List[Try[ScheduledViva]] = {

    vivas.map(viva => ScheduledViva.create(viva, findResourcesMaxedAvailability(viva).get))
  }

  /*
  In this method we go through the availabilites of the resources. We start by looking for the maxed preference of each resource.
  The algorithm keeps looking to the next maxed availabilites until it finds a period of time where all resources are
  simultaneously available.
   */
  def findResourcesMaxedAvailability(viva: Viva): Option[Period] = {

    val maxedPeriods = viva.jury.asResourcesSet
      .flatMap(resource => resource.availabilities)
      .toList
      .sortBy(x => x.preference.value)
      .map(availability => availability.period)
      .map(period => Period.create(period.start, period.start.plus(viva.duration.timeDuration)).get)

    maxedPeriods.find(period => viva.jury.asResourcesSet.forall(resource => resource.isAvailableOn(period)))
  }
}
