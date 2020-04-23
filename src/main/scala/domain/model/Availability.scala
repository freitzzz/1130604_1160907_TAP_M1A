package domain.model

import java.time.LocalDateTime

import scala.util.{Failure, Success, Try}

abstract case class Availability private (period: Period,
                                          preference: Preference) {

  override def equals(o: Any): Boolean = this.hashCode() == o.hashCode()

  override def hashCode(): Int =
    period.hashCode() + preference.value
}

object Availability {
  def create(period: Period, preference: Preference): Try[Availability] =
    Success(new Availability(period, preference) {})
}
