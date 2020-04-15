package domain.model

class Teacher(id: String,
              name: String,
              availabilities: List[Availability],
              roles: List[Role])
    extends Resource(id, name, availabilities, roles) {}
