package domainSchedulerImpl

import domain.model.{
  Period,
  ScheduledViva,
  ScheduledVivaService,
  Viva,
  VivasService
}
import domain.schedule.DomainScheduler

import scala.annotation.tailrec
import scala.util.{Failure, Try}

object MS03Scheduler extends DomainScheduler {
  override def generateScheduledVivas(
    vivas: List[Viva]
  ): List[Try[ScheduledViva]] = {

    val diffAndIntersect = VivasService.differAndIntersect(vivas)

    //for the diff, simply calculate the best availability per resource and return it
    val differencesViva = DiffScheduler.DiffScheduler.ScheduleVivasIndividually(
      diffAndIntersect._1._1.toList
    )

    //for the intersect, apply algorithm

    val scheduledVivas = scheduleVivas(diffAndIntersect._2._1.toList)

    scheduledVivas

    println(scheduledVivas)

    //List[Try[ScheduledViva]]()//remove this once code is completed

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
          val vivaOption = findVivaThatHasTheBiggestSchedulePreference(vivas)

          vivaOption match {
            case Some(value) =>
              val viva = value._3
              val period = value._2

              println(value)

              val updatedVivas =
                ScheduledVivaService.updateVivasAccordingToScheduledVivaPeriod(
                  viva.jury,
                  vivas.filter(_ != viva),
                  period
                )

              val scheduledViva = ScheduledViva
                .create(viva, period)

              auxScheduleVivas(updatedVivas, scheduledViva :: scheduledVivas)
            case None =>
              // TODO: nao poder ser esta mensagem de erro, o head nao e utilizado
              println("!")
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

  private def findVivaThatHasTheBiggestSchedulePreference(
    vivas: List[Viva]
  ): Option[(Int, Period, Viva)] = {

    // TODO: This has to be tail recursive

    def auxFindVivaThatHasTheBiggestSchedulePreference(
      vivas: List[Viva],
      tuples: List[(Int, Period, Viva)]
    ): List[(Int, Period, Viva)] = {

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

            if (period.toString == "Period(2020-06-10T08:00,2020-06-10T10:05)" && headViva.duration.toString == "Duration(PT1H15M)")
              println("!!!!!!!!!!!!")

            val sumOfPreferences = ScheduledVivaService
              .calculateSumOfPreferences(vivaJuryAsResourcesSet, vivaPeriod)

            val updatedVivas = ScheduledVivaService
              .updateVivasAccordingToScheduledVivaPeriod(
                vivaJury,
                tailVivas,
                vivaPeriod
              )

            auxFindVivaThatHasTheBiggestSchedulePreference(
              updatedVivas,
              tuples ++ List((sumOfPreferences, vivaPeriod, headViva))
            )

          })

        case Nil => tuples.reverse
      }

    }

    val maximizedVivas =
      auxFindVivaThatHasTheBiggestSchedulePreference(
        vivas,
        List[(Int, Period, Viva)]()
      )

    println(maximizedVivas.count(_._1 == 15))

    println(maximizedVivas.count(a => a._3.student.s == "V003" && a._1 == 15))

    val a = maximizedVivas
      .sortBy(_._1)
      .reverse

    a.headOption match {
      case Some(value) => {
        val biggestSumOfPreferences = value._1

        a.filter(_._1 == biggestSumOfPreferences)
          .sortBy(_._2.start)
          .sortBy(_._3.title.s)
          .headOption
      }
      case None => None
    }

    /*    println(a)

    a*/

  }
}
