package domain.model

import java.time

import scala.util.{Failure, Success, Try}

sealed abstract case class Duration private (timeDuration: time.Duration) {}

object Duration {

  def create(timeDuration: time.Duration): Try[Duration] =
    if (timeDuration.isNegative || timeDuration.isZero) {
      Failure(
        new IllegalArgumentException(
          "Duration cannot be equal or lower than zero"
        )
      )
    } else {
      Success(new Duration(time.Duration.from(timeDuration)) {})
    }

}
