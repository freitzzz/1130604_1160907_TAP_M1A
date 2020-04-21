package domain.model

import java.time.LocalDateTime

sealed abstract case class ScheduledViva private (viva: Viva,
                                                  start: LocalDateTime,
                                                  end: LocalDateTime,
                                                  scheduledPreference: Int)
object ScheduledViva {

  def create(viva: Viva,
             start: LocalDateTime,
             end: LocalDateTime): Option[ScheduledViva] = {

    val vivaJuryAsResourcesSet = viva.jury.asResourcesSet

    if (end.isBefore(start) || !vivaJuryAsResourcesSet.forall(
          resource => resource.isAvailableOn(start, end)
        )) {
      None
    } else {
      val sumOfPreferences = vivaJuryAsResourcesSet
        .flatMap(resource => resource.availabilityOn(start, end))
        .foldLeft(0)(_ + _.preference)
      Some(new ScheduledViva(viva, start, end, sumOfPreferences) {})
    }

  }

}
