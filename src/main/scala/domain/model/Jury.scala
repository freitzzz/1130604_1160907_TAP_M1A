package domain.model

abstract case class Jury private (president: Resource,
                                  adviser: Resource,
                                  supervisors: List[Resource],
                                  coAdvisers: List[Resource]) {

  override def equals(obj: Any): Boolean = super.equals(obj)

  override def hashCode(): Int =
    president.hashCode() + adviser.hashCode() + supervisors
      .hashCode() + coAdvisers.hashCode()
}

object Jury {
  def create(president: Resource,
             adviser: Resource,
             supervisors: List[Resource],
             coAdvisers: List[Resource]) = {
    if (president.hasRole[President] && adviser.hasRole[Adviser]) {
      Some(new Jury(president, adviser, supervisors, coAdvisers) {})
    } else {
      None
    }
  }
}
