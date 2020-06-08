package domain.model

sealed trait Role {}

case class President() extends Role {}

case class Adviser() extends Role {}

case class CoAdviser() extends Role {}

case class Supervisor() extends Role {}
