import org.scalacheck.Properties

package object property extends Properties("ScheduledViva") {

  property(
    "when resources of a viva are available on the period of the viva, then a scheduled viva is always created"
  ) = {
    true
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
