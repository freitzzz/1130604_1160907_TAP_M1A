package startup

import assessment.AssessmentMS01
import io.FileIO
import xml.Functions

object Main {

  def main(args: Array[String]): Unit = {

    println("Please insert the name of the file to read: ")

    //val inputFile = scala.io.StdIn.readLine()

    println("Please insert the name you want for the output file: ")
    //val outputFileName = scala.io.StdIn.readLine()

    val xml = FileIO.load(
      "/home/freitas/Development/Projects/TAP/1130604_1160907_tap_m1a/files/assessment/ms01/valid_agenda_in.xml"
    )

    val schema = FileIO.load(
      "/home/freitas/Development/Projects/TAP/1130604_1160907_tap_m1a/files/agenda.xsd"
    )

    assert(xml.isSuccess)

    assert(schema.isSuccess)

    val agenda = xml.get

    val agendaSchema = schema.get

    val agendaCompliesWithSchema = Functions.validate(agenda, agendaSchema)

    assert(agendaCompliesWithSchema.isSuccess)

    AssessmentMS01.create(agenda)

  }
}
