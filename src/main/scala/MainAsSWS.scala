import domain.model.Agenda
import domainSchedulerImpl.MS03Scheduler
import io.FileIO
import xml.Functions

object MainAsSWS {

  def main(args: Array[String]): Unit = {
    val elem = FileIO
      .load(
        "/home/freitas/Development/Projects/TAP/1130604_1160907_tap_m1a/files/assessment/ms03/valid_agenda_control_00_in.xml"
      )
      .get

    val vivas = Functions.deserialize(elem).get

    val scheduledVivasTry = MS03Scheduler.generateScheduledVivas(vivas)

    scheduledVivasTry.find(a => a.isFailure)

    val scheduledVivas = scheduledVivasTry.flatMap(a => a.toOption)

    val output = Functions.serialize(Agenda(scheduledVivas))

    println(scheduledVivas)

    FileIO.save("/home/freitas/Desktop/ok2.xml", output)
  }

}
