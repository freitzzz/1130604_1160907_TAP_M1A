package domain.model

import scala.math.Ordering.Implicits._
import scala.util.{Failure, Success, Try}

sealed abstract class Resource(val id: NonEmptyString,
                               val name: NonEmptyString,
                               val availabilities: List[Availability],
                               roles: List[Role]) {

  // O(N)
  def availabilitiesPossibleFor(vivaDuration: Duration): List[Availability] = {

    availabilities.filter(
      a => a.period.end.minus(vivaDuration.timeDuration) >= a.period.start
    )

  }

  def hasRole(role: Role): Boolean = this.roles.contains(role)

  def isAvailableOn(period: Period): Boolean =
    availabilityOn(period).nonEmpty

  def availabilityOn(period: Period): Option[Availability] = {
    availabilities.find(
      availability =>
        period.start >= availability.period.start && period.end <= availability.period.end
    )
  }

  override def equals(o: Any): Boolean = this.hashCode() == o.hashCode()

  override def hashCode(): Int = id.hashCode()

}

object Resource {

  def validResource(id: NonEmptyString,
                    name: NonEmptyString,
                    availabilities: List[Availability],
                    roles: List[Role]): Try[Any] = {

    if (availabilities.distinct.size != availabilities.size)
      Failure(
        new IllegalArgumentException(
          "Resource cannot have duplicate availabilities"
        )
      )
    else if (overlappingPeriodsExist(availabilities))
      Failure(
        new IllegalArgumentException(
          "Resource cannot have overlapping availability periods"
        )
      )
    else if (roles.isEmpty)
      Failure(new IllegalArgumentException("Resource needs at least one role"))
    else if (roles.distinct.size != roles.size)
      Failure(
        new IllegalArgumentException("Resource cannot have duplicate roles")
      )
    else
      Success(null)
  }

  private def overlappingPeriodsExist(
    availabilities: List[Availability]
  ): Boolean = {
    val orderedPeriods = availabilities
      .map(_.period)
      .sortWith((p1, p2) => p1.start.isBefore(p2.start))

    orderedPeriods
      .flatMap(
        p1 =>
          orderedPeriods
            .filter(_.start.isAfter(p1.start))
            .filter(p2 => p1.overlaps(p2))
      )
      .nonEmpty
  }
}

case class Teacher private (override val id: NonEmptyString,
                            override val name: NonEmptyString,
                            override val availabilities: List[Availability],
                            roles: List[Role])
    extends Resource(id, name, availabilities, roles)

object Teacher {
  def create(id: NonEmptyString,
             name: NonEmptyString,
             availabilities: List[Availability],
             roles: List[Role]): Try[Teacher] = {
    val validResource = Resource.validResource(id, name, availabilities, roles)

    if (validResource.isFailure)
      Failure(validResource.failed.get)
    else if (roles.exists(role => role.isInstanceOf[Supervisor]))
      Failure(
        new IllegalArgumentException(
          "Teacher cannot have the role of supervisor"
        )
      )
    else
      Success(new Teacher(id, name, availabilities, roles))
  }
}

case class External private (override val id: NonEmptyString,
                             override val name: NonEmptyString,
                             override val availabilities: List[Availability],
                             roles: List[Role])
    extends Resource(id, name, availabilities, roles)

object External {
  def create(id: NonEmptyString,
             name: NonEmptyString,
             availabilities: List[Availability],
             roles: List[Role]): Try[External] = {
    val validResource = Resource.validResource(id, name, availabilities, roles)

    if (validResource.isFailure)
      Failure(validResource.failed.get)
    else if (roles.exists(role => role.isInstanceOf[President]))
      Failure(
        new IllegalArgumentException(
          "External cannot have the role of president"
        )
      )
    else if (roles.exists(role => role.isInstanceOf[Adviser]))
      Failure(
        new IllegalArgumentException("External cannot have the role of adviser")
      )
    else
      Success(new External(id, name, availabilities, roles))

  }
}
