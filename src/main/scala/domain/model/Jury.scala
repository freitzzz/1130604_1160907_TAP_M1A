package domain.model

case class Jury(president: President,
                adviser: Adviser,
                coAdvisers: List[CoAdviser],
                supervisors: List[Supervisor])
