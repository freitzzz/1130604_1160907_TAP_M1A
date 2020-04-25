package domain.model

import scala.util._

sealed abstract case class ScheduledViva private (viva: Viva,
                                                  period: Period,
                                                  scheduledPreference: Int)
object ScheduledViva {

  def create(viva: Viva, period: Period): Try[ScheduledViva] = {

    val vivaJuryAsResourcesSet = viva.jury.asResourcesSet

    if (!vivaJuryAsResourcesSet.forall(
          resource => resource.isAvailableOn(period)
        )) {
      Failure(
        new IllegalArgumentException(
          s"Invalid scheduled viva creation. Not all members of the jury are available from ${period.start} to ${period.end}."
        )
      )
    } else {
      val sumOfPreferences = vivaJuryAsResourcesSet
        .flatMap(resource => resource.availabilityOn(period))
        .foldLeft(0)(_ + _.preference.value)
      /*
      val resourcesAfterAvailabilityUpdate = vivaJuryAsResourcesSet.map {
        case Teacher(id, name, availabilities, roles) =>
          Teacher.create(id, name, availabilities, roles).get
        case External(id, name, availabilities, roles) =>
          External.create(id, name, availabilities, roles).get
      }*/

      Success(new ScheduledViva(viva, period, sumOfPreferences) {})
    }
  }

  /*private def updateAvailability(availability: Availability,
                                 period: Period): List[Availability] = {

    List(
      Period.create(availability.period.start, period.start),
      Period.create(period.end, availability.period.end)
    ).filter(_.isSuccess)
      .map(
        period => Availability.create(period.get, availability.preference).get
      )
  }

  private def findAvailabilityasdasd (availabilities: List[Availability], period: Period) */
}
