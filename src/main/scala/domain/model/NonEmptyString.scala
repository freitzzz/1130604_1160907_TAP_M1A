package domain.model

import scala.util._

sealed abstract case class NonEmptyString private (s: String)
object NonEmptyString {
  def create(s: String): Try[NonEmptyString] =
    if (s == null) {
      Failure(new IllegalArgumentException("String cannot be null."))
    } else if (s.isEmpty) {
      Failure(new IllegalArgumentException("String cannot be empty."))
    } else
      Success(new NonEmptyString(s) {})

}
