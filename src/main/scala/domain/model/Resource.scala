package domain.model

sealed abstract class Resource(id: String,
                               name: String,
                               availabilities: List[Availability],
                               roles: List[Role]) {}

object Resource {

  def validResource(id: String,
                    name: String,
                    availabilities: List[Availability],
                    roles: List[Role]): Boolean = {
    availabilities.distinct.size == availabilities.size && roles.nonEmpty
  }

}

case class Teacher private (id: String,
                            name: String,
                            availabilities: List[Availability],
                            roles: List[Role])
    extends Resource(id, name, availabilities, roles)

object Teacher {
  def create(id: String,
             name: String,
             availabilities: List[Availability],
             roles: List[Role]): Option[Teacher] = {
    if (Resource.validResource(id, name, availabilities, roles)
        && !roles.exists((role) => role.getClass == Supervisor.getClass))
      Some(new Teacher(id, name, availabilities, roles))
    else
      None
  }
}

case class External private (id: String,
                             name: String,
                             availabilities: List[Availability],
                             roles: List[Role])
    extends Resource(id, name, availabilities, roles)

object External {
  def create(id: String,
             name: String,
             availabilities: List[Availability],
             roles: List[Role]): Option[External] = {
    if (Resource.validResource(id, name, availabilities, roles)
        && !roles.exists(
          (role) =>
            role.getClass == President.getClass || role.getClass == Adviser.getClass
        ))
      Some(new External(id, name, availabilities, roles))
    else
      None
  }
}
