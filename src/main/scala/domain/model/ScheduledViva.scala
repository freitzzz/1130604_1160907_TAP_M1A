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
      Success(new ScheduledViva(viva, period, sumOfPreferences) {})
    }
  }
}
