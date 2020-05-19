package property

import domain.model.Preference
import org.scalacheck.Prop._
import org.scalacheck.{Gen, Properties}

object PreferencePropertyBasedTesting extends Properties("preference") {

  property("preference cannot assume values lower than 1") = {
    forAll(Gen.chooseNum(Int.MinValue, 0)) { value: Int =>
      Preference.create(value).isFailure
    }
  }

  property("preference cannot assume values higher than 5") = {
    forAll(Gen.chooseNum(6, Int.MaxValue)) { value: Int =>
      Preference.create(value).isFailure
    }
  }

  property("preference can assume values in range 1-5") = {
    forAll(Gen.chooseNum(1, 5)) { value: Int =>
      Preference.create(value).isSuccess
    }
  }

}
