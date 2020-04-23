package domain.model

import scala.util.{Failure, Success, Try}

sealed abstract case class Preference private (value: Int) {}

object Preference {

  def create(value: Int): Try[Preference] = {

    if (value <= 0 || value > 5)
      Failure(
        new IllegalArgumentException(
          "Preference can only assume values in [1, 5] range"
        )
      )
    else
      Success(new Preference(value) {})

  }

}
