package domain.model

import java.time.LocalDateTime

abstract case class Availability private (start: LocalDateTime,
                                          end: LocalDateTime,
                                          preference: Int) {

  override def equals(o: Any): Boolean = this.hashCode() == o.hashCode()

  override def hashCode(): Int = start.hashCode() + end.hashCode() + preference
}

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
