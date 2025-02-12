package domain.model

import scala.util._

sealed abstract case class ScheduledViva private (viva: Viva,
                                                  period: Period,
                                                  scheduledPreference: Int)
object ScheduledViva {

  def create(viva: Viva, period: Period): Try[ScheduledViva] = {

    val vivaJuryAsResourcesSet = viva.jury.asResourcesSet

    if (!java.time.Duration
          .between(period.start, period.end)
          .equals(viva.duration.timeDuration)) {
      Failure(
        new IllegalArgumentException(
          s"Invalid period. Period duration should always be the same as the viva to be scheduled."
        )
      )
    } else if (!vivaJuryAsResourcesSet.forall(
                 resource => resource.isAvailableOn(period)
               )) {
      Failure(
        new IllegalArgumentException(
          s"Invalid scheduled viva creation. Not all members of the jury are available from ${period.start} to ${period.end}."
        )
      )
    } else {
      val sumOfPreferences = ScheduledVivaService.calculateSumOfPreferences(
        viva.jury.asResourcesSet,
        period
      )

      Success(new ScheduledViva(viva, period, sumOfPreferences) {})
    }
  }
}
