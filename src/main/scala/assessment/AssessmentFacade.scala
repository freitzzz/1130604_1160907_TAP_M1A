package assessment

import domain.model.{Agenda, ScheduledViva, Viva}
import domain.schedule._
import domainSchedulerImpl.{MS01Scheduler, MS03Scheduler}
import xml.Functions

import scala.util.{Failure, Success, Try}
import scala.xml.Elem

object AssessmentMS01 extends Schedule {
  // TODO: Use the functions in your own code to implement the assessment of ms01
  def create(xml: Elem): Try[Elem] = {
    SchedulerResolver.applyScheduler(xml, MS01Scheduler.generateScheduledVivas)
  }
}

object AssessmentMS03 extends Schedule {
  // TODO: Use the functions in your own code to implement the assessment of ms03
  def create(xml: Elem): Try[Elem] = {
    SchedulerResolver.applyScheduler(xml, MS03Scheduler.generateScheduledVivas)
  }
}

object CustomAssessmentMS03 extends Schedule {
  def create(xml: Elem): Try[Elem] = {
    SchedulerResolver.applyScheduler(xml, MS03Scheduler.generateScheduledVivas)
  }
}

object SchedulerResolver {

  def applyScheduler(
    xml: Elem,
    scheduler: List[Viva] => List[Try[ScheduledViva]]
  ): Try[Elem] = {
    val vivasParse = Functions.deserialize(xml)

    vivasParse match {
      case Failure(exception) => Failure(exception)
      case Success(value) =>
        val vivas = value

        val scheduledVivas = scheduler(vivas)

        scheduledVivas.find(_.isFailure) match {
          case Some(value) => Failure(value.failed.get)
          case None =>
            Success(Functions.serialize(Agenda(scheduledVivas.map(_.get))))
        }
    }
  }
}
