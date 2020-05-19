package property

import domain.model.Duration
import org.scalacheck.Prop._
import org.scalacheck.Properties

object DurationPropertyBasedTesting extends Properties("duration") {

  property("duration cannot assume negative values") = {
    forAll(Generators.genNegativeJavaTimeDuration) {
      duration: java.time.Duration =>
        Duration.create(duration).isFailure
    }
  }

  property("duration can assume positive values") = {
    forAll(Generators.genGreaterThanZeroJavaTimeDuration) {
      duration: java.time.Duration =>
        Duration.create(duration).isSuccess
    }
  }

}
