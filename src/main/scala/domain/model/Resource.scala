package domain.model

import scala.math.Ordering.Implicits._
import scala.util.{Failure, Success, Try}

sealed abstract class Resource(val id: NonEmptyString,
                               name: NonEmptyString,
                               val availabilities: List[Availability],
                               roles: List[Role]) {

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
    else if (roles.isEmpty)
      Failure(new IllegalArgumentException("Resource needs at least one role"))
    else if (roles.distinct.size != roles.size)
      Failure(
        new IllegalArgumentException("Resource cannot have duplicate roles")
      )
    else
      Success(null)
  }

}

case class Teacher private (override val id: NonEmptyString,
                            name: NonEmptyString,
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
                             name: NonEmptyString,
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
