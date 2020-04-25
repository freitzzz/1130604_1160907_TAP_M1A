package startup

import domain.model._
import io.FileIO
import java.time.LocalDateTime

import xml.Validator

import scala.util.{Failure, Success, Try}
import scala.xml.{Node, XML}

object Main {

  def main(args: Array[String]): Unit = {

    println("Please insert the name of the file to read: ")

    //val inputFile = scala.io.StdIn.readLine()

    println("Please insert the name you want for the output file: ")
    //val outputFileName = scala.io.StdIn.readLine()

    val xml = FileIO.load(
      "C:\\Tap\\1130604_1160907_tap_m1a\\files\\assessment\\ms01\\valid_agenda_in.xml"
    )

    val schema =
      FileIO.load("C:\\Tap\\1130604_1160907_tap_m1a\\files\\agenda.xsd")

    assert(xml.isSuccess)

    assert(schema.isSuccess)

    val agenda = xml.get

    val agendaSchema = schema.get

    val agendaCompliesWithSchema = Validator.validate(agenda, agendaSchema)

    assert(agendaCompliesWithSchema.isSuccess)

    // Retrieve Vivas
    val vivasXML = agenda \ "vivas" \ "viva"

    val roles = vivasXML
      .map(
        viva => viva.descendant.filter(node => node.attribute("id").nonEmpty)
      )
      .flatten
      .map(
        node =>
          (node \@ "id", node.label match {
            case "president"  => President()
            case "adviser"    => Adviser()
            case "coadviser"  => CoAdviser()
            case "supervisor" => Supervisor()
          })
      )

    val mappedRoles = roles
      .groupBy(role => role._1)
      .map(entry => (entry._1, entry._2.map(tuple => tuple._2).distinct))

    // Retrieve teachers
    val teachersXML = agenda \ "resources" \ "teachers" \ "teacher"
    val mappedTeachers = teachersXML
      .filter(node => mappedRoles.contains(node \@ "id"))
      .groupMap(keyNode => keyNode \@ "id")(
        valueNode =>
          Teacher
            .create(
              NonEmptyString.create(valueNode \@ "id").get,
              NonEmptyString
                .create(valueNode \@ "name")
                .get,
              (valueNode \ "availability")
                .map(
                  childNode =>
                    Availability
                      .create(
                        Period
                          .create(
                            LocalDateTime
                              .parse(childNode \@ "start"),
                            LocalDateTime
                              .parse(childNode \@ "end")
                          )
                          .get,
                        Preference
                          .create((childNode \@ "preference").toInt)
                          .get
                      )
                      .get
                )
                .toList,
              roles = mappedRoles(valueNode \@ "id").toList
          )
      )
      .toList

    // Retrieve externals
    val externalsXML = agenda \ "resources" \ "externals" \ "external"

    val mappedExternals = externalsXML
      .filter(node => mappedRoles.contains(node \@ "id"))
      .groupMap(keyNode => keyNode \@ "id")(
        valueNode =>
          External
            .create(
              NonEmptyString.create(valueNode \@ "id").get,
              NonEmptyString
                .create(valueNode \@ "name")
                .get,
              (valueNode \ "availability")
                .map(
                  childNode =>
                    Availability
                      .create(
                        Period
                          .create(
                            LocalDateTime
                              .parse(childNode \@ "start"),
                            LocalDateTime
                              .parse(childNode \@ "end")
                          )
                          .get,
                        Preference
                          .create((childNode \@ "preference").toInt)
                          .get
                      )
                      .get
                )
                .toList,
              roles = mappedRoles(valueNode \@ "id").toList
          )
      )
      .toList

    val vivas = vivasXML
      .map(
        node =>
          Viva
            .create(
              NonEmptyString.create(node \@ "student").get,
              NonEmptyString.create(node \@ "title").get,
              Jury
                .create(
                  president = mappedTeachers
                    .find(
                      p =>
                        p._2.head.get.id == (node \ "president")
                          .map(
                            childNode =>
                              NonEmptyString
                                .create(childNode \@ "id")
                                .get
                          )
                          .head
                    )
                    .get
                    ._2
                    .head
                    .get,
                  adviser = mappedTeachers
                    .find(
                      a =>
                        a._2.head.get.id == (node \ "adviser")
                          .map(
                            childNode =>
                              NonEmptyString
                                .create(childNode \@ "id")
                                .get
                          )
                          .head
                    )
                    .get
                    ._2
                    .head
                    .get,
                  supervisors = (node \ "supervisor")
                    .map(
                      childNode =>
                        mappedExternals
                          .find(
                            s =>
                              s._2.head.get.id == NonEmptyString
                                .create(childNode \@ "id")
                                .get
                          )
                          .get
                          ._2
                          .head
                          .get
                    )
                    .toList,
                  coAdvisers = (node \ "coadviser")
                    .map(
                      childNode =>
                        mappedExternals
                          .find(
                            s =>
                              s._2.head.get.id == NonEmptyString
                                .create(childNode \@ "id")
                                .get
                          )
                          .get
                          ._2
                          .head
                          .get
                    )
                    .toList
                )
                .get
            )
            .get
      )
      .toList
  }
}
