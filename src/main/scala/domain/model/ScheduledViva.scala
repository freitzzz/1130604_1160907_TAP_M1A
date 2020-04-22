package domain.model

import java.time.LocalDateTime

import scala.util._

sealed abstract case class ScheduledViva private (viva: Viva,
                                                  start: LocalDateTime,
                                                  end: LocalDateTime,
                                                  scheduledPreference: Int)
object ScheduledViva {

  def create(viva: Viva,
             start: LocalDateTime,
             end: LocalDateTime): Try[ScheduledViva] = {

    val vivaJuryAsResourcesSet = viva.jury.asResourcesSet

    if (end.isBefore(start)) {
      Failure(
        new IllegalArgumentException(
          s"Invalid scheduled viva creation. End date ${end} cannot be before start date ${start}."
        )
      )
    } else if (!vivaJuryAsResourcesSet.forall(
                 resource => resource.isAvailableOn(start, end)
               )) {
      Failure(
        new IllegalArgumentException(
          s"Invalid scheduled viva creation. Not all members of the jury are available from ${start} to ${end}."
        )
      )
    } else {
      val sumOfPreferences = vivaJuryAsResourcesSet
        .flatMap(resource => resource.availabilityOn(start, end))
        .foldLeft(0)(_ + _.preference)
      Success(new ScheduledViva(viva, start, end, sumOfPreferences) {})
    }
  }
}
