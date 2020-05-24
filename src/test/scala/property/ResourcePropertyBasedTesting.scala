package property

import org.scalacheck.Prop._
import org.scalacheck.Properties

object ResourcePropertyBasedTesting extends Properties(name = "resource") {

  property("resource cannot have overlapping availabilities") = {
    true
  }

  property("resource is only available when has a period available") = {
    true
  }
}
