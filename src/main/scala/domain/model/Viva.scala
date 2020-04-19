package domain.model

sealed abstract case class Viva private (student: String,
                                         title: String,
                                         jury: Jury)

object Viva {

  def create(student: String, title: String, jury: Jury): Option[Viva] = {
    // no validations so far to check but necessary to comply with the ADT and Smart Constructors pattern
    Option(new Viva(student, title, jury) {})
  }

}
