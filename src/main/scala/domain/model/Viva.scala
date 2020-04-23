package domain.model

import scala.util._

sealed abstract case class Viva private (student: NonEmptyString,
                                         title: NonEmptyString,
                                         jury: Jury)

object Viva {

  def create(student: NonEmptyString,
             title: NonEmptyString,
             jury: Jury): Try[Viva] = {
    // no validations so far to check but necessary to comply with the ADT and Smart Constructors pattern
    Success(new Viva(student, title, jury) {})
  }

}
