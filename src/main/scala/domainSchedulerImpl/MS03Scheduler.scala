package domainSchedulerImpl

import domain.model._
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
      if (diffAndIntersect._2._1.nonEmpty) {
        val vivasWhichResourcesIntersect =
          diffAndIntersect._2._1
            .map(viva => (viva, vivas.indexOf(viva)))
            .toList
            .sortBy(_._2)
            .map(_._1)
        scheduleVivasThatShareResources(vivasWhichResourcesIntersect)
      } else {
        List()
      }

    val completeSchedule = sharedScheduledVivas ++ isolatedScheduledVivas

    // Order

    completeSchedule.find(_.isFailure) match {
      case Some(_) => completeSchedule
      case None =>
        val scheduledVivasSortedByDateTime =
          completeSchedule.map(_.get).sortBy(_.period.start)

        val scheduledVivasScheduledToSameDateTime =
          scheduledVivasSortedByDateTime
            .groupBy(_.period)
            .filter(_._2.size > 1)
            .flatMap(_._2)

        val orderedScheduledVivas =
          if (scheduledVivasScheduledToSameDateTime.isEmpty) {
            scheduledVivasSortedByDateTime
          } else {
            orderScheduledVivasByVivaTitle(
              scheduledVivasSortedByDateTime.drop(1),
              List[ScheduledViva](scheduledVivasSortedByDateTime.headOption.get)
            )
          }

        orderedScheduledVivas.map(Success(_))

    }
  }

  @tailrec
  private def orderScheduledVivasByVivaTitle(
    scheduledVivas: List[ScheduledViva],
    orderedScheduledVivas: List[ScheduledViva]
  ): List[ScheduledViva] = {
    scheduledVivas match {
      case ::(head, next) =>
        val beforeScheduledViva = orderedScheduledVivas.lastOption.get

        if (head.period == beforeScheduledViva.period) {

          if (beforeScheduledViva.viva.title.s < head.viva.title.s) {
            orderScheduledVivasByVivaTitle(
              next,
              orderedScheduledVivas ++ List(head)
            )
          } else {
            orderScheduledVivasByVivaTitle(
              next,
              orderedScheduledVivas
                .filter(_ != beforeScheduledViva) ++ List(
                head,
                beforeScheduledViva
              )
            )
          }
        } else {
          orderScheduledVivasByVivaTitle(
            next,
            orderedScheduledVivas ++ List(head)
          )
        }

      case Nil => orderedScheduledVivas
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
            .map(_._1.map(_.get))
            .map(combination => (combination.map(_.period), combination))
            .sortBy(_._1.toString())
            .map(_._2)

        val vivasThatShareSameJury =
          VivasService.findVivasThatShareTheSameJury(vivas)

        val scheduledVivas = if (vivasThatShareSameJury.isEmpty) {

          vivasScheduleCombinationsWithHighestSumOfPreferences.headOption.get

        } else {

          // if any vivas being scheduled share the same jury, then the picking of the best scheduled preference
          // must be based on the order of the vivas input

          findBestCombinationsAccordingToVivasThatShareTheSameJury(
            vivasScheduleCombinationsWithHighestSumOfPreferences,
            vivasThatShareSameJury.toList
          ).headOption.get
        }

        scheduledVivas.map(Success(_))
      case None =>
        List(Failure(new IllegalStateException("Schedule is impossible")))
    }

  }

  @scala.annotation.tailrec
  private def findBestCombinationsAccordingToVivasThatShareTheSameJury(
    combinations: List[List[ScheduledViva]],
    vivasThatShareSameJury: List[(Set[Resource], List[Viva])]
  ): List[List[ScheduledViva]] = vivasThatShareSameJury match {
    case ::(head, next) =>
      val vivaWithPriority = head._2.headOption.get

      val bestSchedulePreferenceInCombinationsForVivaWithPriority =
        combinations.flatten
          .filter(_.viva.title == vivaWithPriority.title)
          .maxByOption(_.scheduledPreference)
          .get

      val bestCombinationsForVivaWithPriority = combinations.filter(
        scheduledVivas =>
          scheduledVivas.exists(
            scheduledViva =>
              scheduledViva.viva.title == vivaWithPriority.title && scheduledViva.scheduledPreference == bestSchedulePreferenceInCombinationsForVivaWithPriority.scheduledPreference
        )
      )

      findBestCombinationsAccordingToVivasThatShareTheSameJury(
        bestCombinationsForVivaWithPriority,
        next
      )
    case Nil => combinations
  }
}
