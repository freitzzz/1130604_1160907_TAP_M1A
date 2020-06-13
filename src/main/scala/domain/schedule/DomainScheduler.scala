package domain.schedule

import domain.model.{ScheduledViva, Viva}

import scala.util.Try

trait DomainScheduler {

  def scheduleVivas(vivas: List[Viva]): List[Try[ScheduledViva]]
}
