package domain.model

class External(id: String,
               name: String,
               availabilities: List[Availability],
               roles: List[Role])
    extends Resource(id, name, availabilities, roles) {}
