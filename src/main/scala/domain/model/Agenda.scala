package domain.model

sealed case class Agenda private (scheduledVivas: List[ScheduledViva])

object Agenda {

  def create(scheduledVivas: List[ScheduledViva]): Agenda = {
    new Agenda(scheduledVivas)
  }

}
