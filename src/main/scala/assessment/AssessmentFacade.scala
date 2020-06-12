package assessment

import java.time.Duration

import domain.model.{
  Agenda,
  Availability,
  External,
  Jury,
  Period,
  Resource,
  ScheduledViva,
  Teacher,
  Viva
}
import domain.schedule._
import domainSchedulerImpl.{MS01Scheduler, MS03Scheduler}
import xml.Functions

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}
import scala.xml.Elem

object AssessmentMS01 extends Schedule {
  // TODO: Use the functions in your own code to implement the assessment of ms01
  def create(xml: Elem): Try[Elem] = {
    val vivasParse = Functions.deserialize(xml)

    vivasParse match {
      case Failure(exception) => Failure(exception)
      case Success(value) =>
        val vivas = value

        val scheduledVivas = MS01Scheduler.generateScheduledVivas(vivas)

        scheduledVivas.find(_.isFailure) match {
          case Some(value) => Failure(value.failed.get)
          case None =>
            Success(Functions.serialize(Agenda(scheduledVivas.map(_.get))))
        }
    }
  }
}

object AssessmentMS03 extends Schedule {
  // TODO: Use the functions in your own code to implement the assessment of ms03
  def create(xml: Elem): Try[Elem] = {
    val vivasParse = Functions.deserialize(xml)

    vivasParse match {
      case Failure(exception) => Failure(exception)
      case Success(value) =>
        val vivas = value

        val scheduledVivas = MS03Scheduler.generateScheduledVivas(vivas)

        scheduledVivas.find(_.isFailure) match {
          case Some(value) => Failure(value.failed.get)
          case None =>
            Success(Functions.serialize(Agenda(scheduledVivas.map(_.get))))
        }
    }
  }
}
