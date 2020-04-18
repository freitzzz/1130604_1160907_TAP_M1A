package domain.model

abstract case class Jury private (president: Resource,
                                  adviser: Resource,
                                  supervisors: List[Resource],
                                  coAdvisers: List[Resource]) {

  override def equals(obj: Any): Boolean = super.equals(obj)

  override def hashCode(): Int =
    president.hashCode() + adviser.hashCode() + supervisors
      .hashCode() + coAdvisers.hashCode()

  def asResourcesSet(): Set[Resource] = {
    val supervisorSet = supervisors.toSet
    val coAdvisersSet = coAdvisers.toSet
    val presidentsSet = Set(president)
    val advisersSet = Set(adviser)

    supervisorSet ++ coAdvisersSet ++ presidentsSet ++ advisersSet
  }
}

object Jury {
  def create(president: Resource,
             adviser: Resource,
             supervisors: List[Resource],
             coAdvisers: List[Resource]) = {
    if (president.hasRole(President()) && adviser.hasRole(Adviser()) && supervisors
          .forall(s => s.hasRole(Supervisor())) && coAdvisers
          .forall(co => co.hasRole(CoAdviser()))) {
      Some(new Jury(president, adviser, supervisors, coAdvisers) {})
    } else {
      None
    }
  }
}
