package domainSchedulerImpl

import domain.model.{
  Period,
  ScheduledViva,
  ScheduledVivaService,
  Viva,
  VivasService
}
import domain.schedule.DomainScheduler

import scala.util.{Failure, Try}

object MS03Scheduler extends DomainScheduler {
  override def generateScheduledVivas(
    vivas: List[Viva]
  ): List[Try[ScheduledViva]] = {

    val diffAndIntersect = VivasService.differAndIntersect(vivas)

    //for the diff, simply calculate the best availability per resource and return it
    val differencesViva = ScheduledVivaService.ScheduleVivasIndividually(
      diffAndIntersect._1._1.toList
    )

    //for the intersect, apply algorithm

    val scheduledVivas = scheduleVivas(diffAndIntersect._2._1.toList)

    val completeSchedule = scheduledVivas ++ differencesViva

    // Order

    completeSchedule.find(_.isFailure) match {
      case Some(_) => completeSchedule
      case None =>
        completeSchedule.sortWith(
          (a, b) => a.get.period.start.isBefore(b.get.period.start)
        )
    }

  }

  private def scheduleVivas(vivas: List[Viva]): List[Try[ScheduledViva]] = {
    findScheduleThatHasTheBiggestSumOfSchedulePreference(vivas)
  }

  private def findScheduleThatHasTheBiggestSumOfSchedulePreference(
    vivas: List[Viva]
  ): List[Try[ScheduledViva]] = {

    // TODO: This has to be tail recursive

    def auxFindVivaThatHasTheBiggestSchedulePreference(
      vivas: List[Viva],
      tuples: List[(List[Try[ScheduledViva]], Int)],
      previousTreeBranch: List[Try[ScheduledViva]] = List[Try[ScheduledViva]](),
      previousTreeSum: Int = 0
    ): List[(List[Try[ScheduledViva]], Int)] = {

      vivas match {
        case ::(headViva, tailVivas) =>
          val possibleVivaPeriods = ScheduledVivaService
            .findPeriodsInWhichVivaCanOccur(headViva)
            .toList

          val vivaJury = headViva.jury
          val vivaJuryAsResourcesSet = vivaJury.asResourcesSet

          possibleVivaPeriods.flatMap(period => {

            val vivaPeriod = Period
              .create(
                period.start,
                period.start.plus(headViva.duration.timeDuration)
              )
              .get

            val sumOfPreferences = ScheduledVivaService
              .calculateSumOfPreferences(vivaJuryAsResourcesSet, vivaPeriod)

            val updatedVivas = ScheduledVivaService
              .updateVivasAccordingToScheduledVivaPeriod(
                vivaJury,
                tailVivas,
                vivaPeriod
              )

            val newTreeBranch = previousTreeBranch ++ List[Try[ScheduledViva]](
              ScheduledViva.create(headViva, vivaPeriod)
            )

            val newTreeSum = previousTreeSum + sumOfPreferences

            auxFindVivaThatHasTheBiggestSchedulePreference(
              updatedVivas,
              tuples ++ List((newTreeBranch, newTreeSum)),
              newTreeBranch,
              newTreeSum
            )

          })

        case Nil => tuples.reverse
      }

    }

    val maximizedVivas =
      auxFindVivaThatHasTheBiggestSchedulePreference(
        vivas,
        List[(List[Try[ScheduledViva]], Int)]()
      )

    val maxSumOfSchedulePreferences =
      maximizedVivas.sortBy(_._2).reverse.headOption

    maxSumOfSchedulePreferences match {
      case Some(value) =>
        maximizedVivas
          .filter(_._2 == value._2)
          .filter(tuple => !tuple._1.exists(_.isFailure))
          .sortWith((a, b) => a._1.toString() < b._1.toString())
          .headOption
          .get
          ._1
      case None =>
        List(
          Failure(
            new IllegalStateException("No shared vivas could be scheduled.")
          )
        )
    }

  }
}
