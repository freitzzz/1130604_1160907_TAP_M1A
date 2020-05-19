package property

import domain.model.Period
import org.scalacheck.Prop._
import org.scalacheck.Properties

object PeriodPropertyBasedTesting extends Properties("period") {

  property("period cannot be described with start date being after end date") = {
    forAll(Generators.genNegativePeriodOfTime) {
      case (start, end) => Period.create(start, end).isFailure
    }
  }

  property(
    "period cannot be described with start date being equal to end date"
  ) = {
    forAll(Generators.genEqualPeriodOfTime) {
      case (start, end) => Period.create(start, end).isFailure
    }
  }

  property("period can be described with start date being before end date") = {
    forAll(Generators.genPositivePeriodOfTime) {
      case (start, end) => Period.create(start, end).isSuccess
    }
  }

  property("period do not overlap with lower period of time") = {
    forAll(Generators.genPeriod) { period: Period =>
      forAll(Generators.genPeriodOfTimeLowerThan(period.start)) {
        case (start, end) => !period.overlaps(Period.create(start, end).get)
      }
    }
  }

  property("period do not overlap with higher period of time") = {
    forAll(Generators.genPeriod) { period: Period =>
      forAll(Generators.genPeriodOfTimeHigherThan(period.end)) {
        case (start, end) => !period.overlaps(Period.create(start, end).get)
      }
    }
  }

  property("period do overlap with period that relies between the period") = {
    forAll(Generators.genPeriod) { period: Period =>
      forAll(Generators.genPeriodOfTimeBetween(period.start, period.end)) {
        case (start, end) => period.overlaps(Period.create(start, end).get)
      }
    }
  }
}
