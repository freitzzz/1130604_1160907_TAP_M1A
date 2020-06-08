package domain.model

sealed abstract case class Viva private (student: NonEmptyString,
                                         title: NonEmptyString,
                                         jury: Jury,
                                         duration: Duration)

object Viva {

  def create(student: NonEmptyString,
             title: NonEmptyString,
             jury: Jury,
             duration: Duration): Viva = {
    new Viva(student, title, jury, duration) {}
  }

}
