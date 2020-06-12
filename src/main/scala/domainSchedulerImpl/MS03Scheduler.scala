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

    println(diffAndIntersect._2)
    println(diffAndIntersect._1)

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
            println("asdasd")
            println(scheduledVivasSortedByDateTime)
            val asd = orderScheduledVivasByVivaTitle(
              scheduledVivasSortedByDateTime.tail,
              List[ScheduledViva](scheduledVivasSortedByDateTime.headOption.get)
            )

            println(asd)

            asd
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
      case ::(head, next) => {

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

    /* println("Maximized Vivas")

    maximizedVivas.foreach(println(_))
     */
    val maxSumOfSchedulePreferences =
      maximizedVivas.sortBy(_._2).reverse.headOption

    maxSumOfSchedulePreferences match {
      case Some(value) =>
        val vivasScheduleCombinationsWithHighestSumOfPreferences =
          maximizedVivas
            .filter(_._2 == value._2)
            .filter(tuple => !tuple._1.exists(_.isFailure))
            .map(_._1.map(_.get))
            .map((combination) => (combination.map(_.period), combination))
            .sortBy(_._1.toString())
            .map(_._2)

        println("vivasScheduleCombinationsWithHighestSumOfPreferences")

        println(
          vivasScheduleCombinationsWithHighestSumOfPreferences.zipWithIndex
            .foreach(a => {

              println(s"Combination ${a._2}")

              a._1.foreach(
                b =>
                  println(
                    s"Viva: ${b.viva.title} Scheduled Preference: ${b.scheduledPreference}} Period: ${b.period}"
                )
              )

            })
        )

        val vivasThatShareSameJury =
          VivasService.findVivasThatShareTheSameJury(vivas)

        val scheduledVivas = if (vivasThatShareSameJury.isEmpty) {

          vivasScheduleCombinationsWithHighestSumOfPreferences.headOption.get

        } else {

          println("Vivas Input Order")

          vivas.foreach(println(_))

          println("Vivas that share the same jury")

          vivasThatShareSameJury.foreach(println(_))

          println("Vivas Schedule Combinations With Highest Sum Of Preferences")

          println(
            vivasScheduleCombinationsWithHighestSumOfPreferences.zipWithIndex
              .foreach(a => {

                println(s"Combination ${a._2}")

                a._1.foreach(
                  b =>
                    println(
                      s"Viva: ${b.viva.title} Scheduled Preference: ${b.scheduledPreference}} Period: ${b.period}"
                  )
                )

              })
          )

          val firstViva =
            vivasThatShareSameJury
              .map(viva => (viva, vivas.indexOf(viva)))
              .toList
              .sortBy(_._2)
              .headOption
              .get
              ._1

          println("First Viva")

          println(firstViva)

          // if any vivas being scheduled share the same jury, then the picking of the best scheduled preference
          // must be based on the order of the vivas input

          /*val b = vivasScheduleCombinationsWithHighestSumOfPreferences
            .map(c => c.sortBy(sc => -sc.scheduledPreference))
            .find(_.headOption.get.viva.title == firstViva.title)*/

          /*println("b")

          println(b.size)

          b.foreach(println(_))

          println(firstViva.title)*/

          val flatted =
            vivasScheduleCombinationsWithHighestSumOfPreferences.flatten

          val biggestSchedulePreferencesForEachViva =
            vivasThatShareSameJury.map(
              viva =>
                (
                  viva,
                  flatted
                    .filter(_.viva.title == viva.title)
                    .maxBy(_.scheduledPreference)
              )
            )

          println("biggest")

          biggestSchedulePreferencesForEachViva.zipWithIndex.foreach(asd => {
            println(
              s"Viva: ${asd._1._1.title} Scheduled Preference: ${asd._1._2.scheduledPreference}}"
            )
          })

          val c = vivasScheduleCombinationsWithHighestSumOfPreferences.find(
            _.contains(
              biggestSchedulePreferencesForEachViva
                .find(_._1.title == firstViva.title)
                .get
                ._2
            )
          )

          val a = vivasScheduleCombinationsWithHighestSumOfPreferences
            .map(c => c.sortBy(sc => -sc.scheduledPreference))
            .headOption
          //.get

          c.get
        }

        scheduledVivas.map(Success(_))
      case None =>
        List(Failure(new IllegalStateException("Schedule is impossible")))
    }

  }

}
