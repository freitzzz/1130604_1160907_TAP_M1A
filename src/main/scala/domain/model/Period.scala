package domain.model

import java.time.LocalDateTime

import scala.math.Ordering.Implicits._

import scala.util.{Failure, Success, Try}

sealed abstract case class Period private (start: LocalDateTime,
                                           end: LocalDateTime) {

  // Check whether a given period of time overlaps with the called Period instance
  def overlaps(period: Period): Boolean = {
    (this.end >= period.start && this.end <= period.end) || (this.start <= period.start && this.end >= period.end)
  }
}

object Period {

  def create(start: LocalDateTime, end: LocalDateTime): Try[Period] = {
    if (end.isBefore(start) || end.isEqual(start))
      Failure(
        new IllegalArgumentException(
          "Period start date time cannot be after the end date time"
        )
      )
    else
      Success(new Period(start, end) {})
  }
}
