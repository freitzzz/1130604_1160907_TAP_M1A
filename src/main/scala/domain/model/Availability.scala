package domain.model

import java.time.LocalDateTime

abstract case class Availability private (start: LocalDateTime,
                                          end: LocalDateTime,
                                          preference: Int)
object Availability {
  def create(start: LocalDateTime,
             end: LocalDateTime,
             preference: Int): Option[Availability] = {
    if (preference <= 0 || preference > 10 || end.isBefore(start))
      None
    else
      Some(new Availability(start, end, preference) {})
  }
}
