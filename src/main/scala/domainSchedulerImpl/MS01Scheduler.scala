package domainSchedulerImpl

import domain.model._
import domain.schedule.DomainScheduler

import scala.annotation.tailrec
import scala.util.{Failure, Try}

object MS01Scheduler extends DomainScheduler {
  override def generateScheduledVivas(
    vivas: List[Viva]
  ): List[Try[ScheduledViva]] = {

    val scheduledVivas = scheduleVivas(vivas)

    scheduledVivas
  }

  private def scheduleVivas(vivas: List[Viva]): List[Try[ScheduledViva]] = {

    @tailrec
    def auxScheduleVivas(
      vivas: List[Viva],
      scheduledVivas: List[Try[ScheduledViva]]
    ): List[Try[ScheduledViva]] = {

      vivas match {
        case ::(head, next) =>
          val periodOption = findFirstPeriodWhichAllResourcesAreAvailable(head)

          periodOption match {
            case Some(value) =>
              val updatedVivas =
                ScheduledVivaService.updateVivasAccordingToScheduledVivaPeriod(
                  head.jury,
                  next,
                  value
                )

              val scheduledViva = ScheduledViva
                .create(head, value)

              auxScheduleVivas(updatedVivas, scheduledViva :: scheduledVivas)
            case None =>
              List[Try[ScheduledViva]](
                Failure(
                  new IllegalStateException(
                    s"Not all Jury elements share a compatible availability. Viva ${head.title.s} could not be scheduled."
                  )
                )
              )
          }
        case Nil => scheduledVivas.reverse
      }

    }

    auxScheduleVivas(vivas, List[Try[ScheduledViva]]())
  }

  private def findFirstPeriodWhichAllResourcesAreAvailable(
    viva: Viva
  ): Option[Period] = {

    val ordPeriods = viva.jury.asResourcesSet
      .flatMap(resource => resource.availabilities)
      .toList
      .sortWith((a1, a2) => a1.period.start.isBefore(a2.period.start))
      .map(availability => availability.period)
      .map(
        period =>
          Period
            .create(period.start, period.start.plus(viva.duration.timeDuration))
            .get
      )

    ordPeriods
      .find(
        period =>
          viva.jury.asResourcesSet
            .forall(resource => resource.isAvailableOn(period))
      )

  }
}
