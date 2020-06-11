package domainSchedulerImpl

import java.util.UUID

import domain.model.{
  Period,
  ScheduledViva,
  ScheduledVivaService,
  Viva,
  VivasService
}
import domain.schedule.DomainScheduler

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

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

    scheduledVivas

    println(scheduledVivas)

    //List[Try[ScheduledViva]]()//remove this once code is completed

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
      tuples: List[(Int, Period, Viva, Int, List[Try[ScheduledViva]], Int)],
      level: Int = 0,
      previousTreeBranch: List[Try[ScheduledViva]] = List[Try[ScheduledViva]](),
      previousTreeSum: Int = 0
    ): List[(Int, Period, Viva, Int, List[Try[ScheduledViva]], Int)] = {

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

            println(vivaPeriod)

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
              tuples ++ List(
                (
                  sumOfPreferences,
                  vivaPeriod,
                  headViva,
                  level + 1,
                  newTreeBranch,
                  newTreeSum
                )
              ),
              level + 1,
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
        List[(Int, Period, Viva, Int, List[Try[ScheduledViva]], Int)]()
      )

    // This might not be our desired option as we want the one with the best sum of schedule preferences & starts with less time & viva title is "less"
    val bestSchedule = maximizedVivas
      .sortBy(r => (-r._6, r._3.title.s))
      .headOption

    bestSchedule match {
      case Some(value) => value._5
      case None =>
        List(Failure(new IllegalStateException("Couldn't find best schedule")))
    }

  }
}
