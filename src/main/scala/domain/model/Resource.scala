package domain.model

abstract class Resource(id: String,
                        name: String,
                        availabilities: List[Availability],
                        roles: List[Role])
