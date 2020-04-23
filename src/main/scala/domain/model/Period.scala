package domain.model

import java.time.LocalDateTime

import scala.util.{Failure, Success, Try}

sealed abstract case class Period private (start: LocalDateTime,
                                           end: LocalDateTime) {}

object Period {

  def create(start: LocalDateTime, end: LocalDateTime): Try[Period] = {

    if (end.isBefore(start))
      Failure(
        new IllegalArgumentException(
          "Period start date time cannot be after the end date time"
        )
      )
    else
      Success(new Period(start, end) {})
  }

}
