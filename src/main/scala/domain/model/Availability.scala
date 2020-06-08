package domain.model

sealed abstract case class Availability private (period: Period,
                                                 preference: Preference) {

  override def equals(o: Any): Boolean = this.hashCode() == o.hashCode()

  override def hashCode(): Int =
    period.hashCode() + preference.value
}

object Availability {
  def create(period: Period, preference: Preference): Availability =
    new Availability(period, preference) {}
}
