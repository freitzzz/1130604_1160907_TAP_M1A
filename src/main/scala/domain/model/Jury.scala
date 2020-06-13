package domain.model

import scala.util._

abstract case class Jury private (president: Resource,
                                  adviser: Resource,
                                  supervisors: List[Resource],
                                  coAdvisers: List[Resource]) {

  override def equals(obj: Any): Boolean = super.equals(obj)

  override def hashCode(): Int =
    president.hashCode() + adviser.hashCode() + supervisors
      .hashCode() + coAdvisers.hashCode()

  lazy val asResourcesSet: Set[Resource] = {
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
             coAdvisers: List[Resource]): Try[Jury] = {
    if (!president.hasRole(President())) {
      Failure(
        new IllegalArgumentException(
          s"Jury cannot be constituted without a president. The sent president: $president does not have the associated role."
        )
      )
    } else if (!adviser.hasRole(Adviser())) {
      Failure(
        new IllegalArgumentException(
          s"Jury cannot be constituted without an adviser. The sent adviser $adviser does not have the associated role."
        )
      )
    } else if (!supervisors.forall(s => s.hasRole(Supervisor()))) {
      Failure(
        new IllegalArgumentException(
          "Illegal list of supervisors to constitute the jury. Not all resources have the role of supervisor."
        )
      )
    } else if (!coAdvisers.forall(co => co.hasRole(CoAdviser()))) {
      Failure(
        new IllegalArgumentException(
          "Illegal list of co advisers to constitute the jury. Not all resources have the role of co advisor."
        )
      )
    } else if (HasDuplicatedResources(
                 president,
                 adviser,
                 supervisors,
                 coAdvisers
               )) {
      Failure(
        new IllegalArgumentException(
          "Illegal list of resources. Duplicated elements have been found."
        )
      )
    } else
      Success(new Jury(president, adviser, supervisors, coAdvisers) {})
  }

  private def HasDuplicatedResources(president: Resource,
                                     adviser: Resource,
                                     supervisors: List[Resource],
                                     coAdvisers: List[Resource]): Boolean = {

    val allResources =
      adviser :: president :: supervisors ::: coAdvisers

    val distinctResources = allResources.distinct

    allResources.size != distinctResources.size
  }
}
