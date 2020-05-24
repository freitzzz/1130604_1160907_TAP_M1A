import java.time.LocalDateTime

import domain.model.{Period, ScheduledViva}
import org.scalacheck.{Prop, Properties}
import property.Generators

object ScheduledVivaPropertyBasedTesting extends Properties("ScheduledViva") {

  property(
    "when resources of a viva are available on the period of the viva, then a scheduled viva is always created"
  ) = {
    val asd = LocalDateTime.now()
    val period = Period
      .create(asd, asd.plusMinutes(1))
      .get

    Prop.forAll(Generators.genVivaWith(period, 1)) { (viva) =>
      ScheduledViva.create(viva, period).isSuccess
    }

  }

  property(
    "sum of all preferences of the resources of a viva should always be positive"
  ) = {
    true
  }

  property(
    "sum of all preferences of the resources should never be higher than number of resources * 5"
  ) = {
    true
  }

  // have in consideration entry values for availabilities that are duplicated
}
