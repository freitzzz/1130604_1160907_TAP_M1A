import domain.model.Agenda
import domainSchedulerImpl._
import io.FileIO
import xml.Functions

val elem = FileIO.load("/home/freitas/Development/Projects/TAP/1130604_1160907_tap_m1a/files/assessment/ms03/valid_agenda_control_01_in.xml").get

val vivas =  Functions.deserialize(elem).get

val scheduledVivasTry = MS03Scheduler.generateScheduledVivas(vivas)

scheduledVivasTry.find(a => a.isFailure)

val scheduledVivas =  scheduledVivasTry.flatMap(a => a.toOption)

val output = Functions.serialize(Agenda(scheduledVivas))

FileIO.save("/home/freitas/Desktop/aaaa.xml", output)