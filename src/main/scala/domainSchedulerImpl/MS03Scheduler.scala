package domainSchedulerImpl

import domain.model.{ScheduledViva, Viva, VivasService}
import domain.schedule.DomainScheduler

import scala.util.Try

object MS03Scheduler extends DomainScheduler{
  override def generateScheduledVivas(vivas: List[Viva]): List[Try[ScheduledViva]] = {

    val diffAndIntersect = VivasService.differAndIntersect(vivas)

    //for the diff, simply calculate the best availability per resource and return it
    val differencesViva = DiffScheduler.DiffScheduler.ScheduleVivasIndividually(diffAndIntersect._1._1.toList)

    //for the intersect, apply algorithm

    List[Try[ScheduledViva]]()//remove this once code is completed
  }
}
