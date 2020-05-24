package domain.schedule

import domain.model.{ScheduledViva, Viva}

import scala.util.Try

trait DomainScheduler {

  def generateScheduledVivas(vivas: List[Viva]): List[Try[ScheduledViva]]
}
