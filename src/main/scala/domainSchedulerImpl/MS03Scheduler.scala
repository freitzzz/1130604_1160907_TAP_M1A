package domainSchedulerImpl

import domain.model.{
  Jury,
  Period,
  Resource,
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
    val isolatedScheduledVivas = ScheduledVivaService.ScheduleVivasIndividually(
      diffAndIntersect._1._1.toList
    )

    //for the intersect, apply algorithm

    val sharedScheduledVivas =
      if (diffAndIntersect._2._1.nonEmpty)
        scheduleVivasThatShareResources(diffAndIntersect._2._1.toList)
      else List()

    val completeSchedule = sharedScheduledVivas ++ isolatedScheduledVivas

    // Order

    completeSchedule.find(_.isFailure) match {
      case Some(_) => completeSchedule
      case None =>
        completeSchedule.sortWith(
          (a, b) => a.get.period.start.isBefore(b.get.period.start)
        )
    }

  }

  private def scheduleVivasThatShareResources(
    vivas: List[Viva]
  ): List[Try[ScheduledViva]] = {
    findScheduleThatHasTheBiggestSumOfSchedulePreference(vivas)
  }

  private def findScheduleThatHasTheBiggestSumOfSchedulePreference(
    vivas: List[Viva]
  ): List[Try[ScheduledViva]] = {

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

          scheduleVivaItsPossiblePeriods(
            possibleVivaPeriods,
            headViva,
            vivaJuryAsResourcesSet,
            vivaJury,
            tailVivas,
            tuples,
            previousTreeBranch,
            previousTreeSum,
            List()
          )

        case Nil => tuples.reverse
      }

    }

    @tailrec
    def scheduleVivaItsPossiblePeriods(
      periods: List[Period],
      headViva: Viva,
      vivaJuryAsResourcesSet: Set[Resource],
      vivaJury: Jury,
      tailVivas: List[Viva],
      tuples: List[(List[Try[ScheduledViva]], Int)],
      previousTreeBranch: List[Try[ScheduledViva]],
      previousTreeSum: Int,
      scheduledVivas: List[(List[Try[ScheduledViva]], Int)]
    ): List[(List[Try[ScheduledViva]], Int)] = {
      periods match {
        case ::(period, next) =>
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

          val temp = auxFindVivaThatHasTheBiggestSchedulePreference(
            updatedVivas,
            tuples ++ List((newTreeBranch, newTreeSum)),
            newTreeBranch,
            newTreeSum
          )

          scheduleVivaItsPossiblePeriods(
            next,
            headViva,
            vivaJuryAsResourcesSet,
            vivaJury,
            tailVivas,
            tuples,
            previousTreeBranch,
            previousTreeSum,
            scheduledVivas ++ temp
          )

        case Nil => scheduledVivas.reverse
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
        val vivasScheduleCombinationsWithHighestSumOfPreferences =
          maximizedVivas
            .filter(_._2 == value._2)
            .filter(tuple => !tuple._1.exists(_.isFailure))
            .map(_._1.map(_.get).sortBy(_.viva.title.s))

        val vivasThatShareSameJury =
          VivasService.findVivasThatShareTheSameJury(vivas)

        val scheduledVivas = if (vivasThatShareSameJury.isEmpty) {

          vivasScheduleCombinationsWithHighestSumOfPreferences.headOption.get

        } else {

          // if any vivas being scheduled share the same jury, then the picking of the best scheduled preference
          // must be based on the order of the vivas input

          vivasScheduleCombinationsWithHighestSumOfPreferences
            .map(c => c.sortBy(sc => -sc.scheduledPreference))
            .find(_.headOption.get.viva.title == vivas.headOption.get.title)
            .get
        }

        scheduledVivas.map(Success(_))
      case None =>
        List(Failure(new IllegalStateException("Schedule is impossible")))
    }

  }
}
