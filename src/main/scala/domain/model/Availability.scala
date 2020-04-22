package domain.model

import java.time.LocalDateTime

import scala.util.{Failure, Success, Try}

abstract case class Availability private (start: LocalDateTime,
                                          end: LocalDateTime,
                                          preference: Int) {

  override def equals(o: Any): Boolean = this.hashCode() == o.hashCode()

  override def hashCode(): Int = start.hashCode() + end.hashCode() + preference
}

object Availability {
  def create(start: LocalDateTime,
             end: LocalDateTime,
             preference: Int): Try[Availability] = {
    if (preference <= 0 || preference > 5 || end.isBefore(start))
      Failure(
        new IllegalArgumentException(
          "#todo: this exception name will not be valuable as refactor of preference and date times to their own classes"
        )
      )
    else
      Success(new Availability(start, end, preference) {})
  }
}
