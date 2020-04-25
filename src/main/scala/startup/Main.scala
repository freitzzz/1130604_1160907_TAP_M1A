package startup

import domain.model._
import io.FileIO
import java.time.LocalDateTime

import xml.Validator

import scala.util.{Failure, Try}
import scala.xml.XML

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

    val agendaCompliesWithSchema = Validator.validate(agenda, agendaSchema)

    assert(agendaCompliesWithSchema.isSuccess)

    // Retrieve Vivas
    val vivasXML = agenda \ "vivas" \ "viva"

    val roles = vivasXML
      .map(viva => viva.child)
      .flatten
      .filter(node => node.label != "#PCDATA")
      .map(
        node =>
          (node.attributes("id").toString(), node.label match {
            case "president"  => Some(President())
            case "adviser"    => Some(Adviser())
            case "coadviser"  => Some(CoAdviser())
            case "supervisor" => Some(Supervisor())
            case _            => None
          })
      )

    val mappedRoles = roles
      .groupBy(role => role._1)
      .map(entry => (entry._1, entry._2.map(tuple => tuple._2.get).distinct))

    println(mappedRoles)

    // Retrieve teachers
    val teachersXML = agenda \ "resources" \ "teachers" \ "teacher"
    val mappedTeachers = teachersXML
      .groupMap(keyNode => keyNode.attributes("id").toString())(
        valueNode =>
          Teacher
            .create(
              NonEmptyString.create(valueNode.attributes("id").toString()).get,
              NonEmptyString
                .create(valueNode.attributes("name").toString())
                .get,
              (valueNode \ "availability")
                .map(
                  childNode =>
                    Availability
                      .create(
                        Period
                          .create(
                            LocalDateTime
                              .parse(childNode.attributes("start").toString()),
                            LocalDateTime
                              .parse(childNode.attributes("end").toString())
                          )
                          .get,
                        Preference
                          .create(
                            childNode.attributes("preference").toString().toInt
                          )
                          .get
                      )
                      .get
                )
                .toList,
              roles = mappedRoles
                .getOrElse(valueNode.attributes("id").toString(), List.empty)
                .toList
          )
      )
      .toList

    println(mappedTeachers)

    // Retrieve externals
    val externalsXML = agenda \ "resources" \ "externals" \ "external"

    val mappedExternals = externalsXML
      .groupMap(keyNode => keyNode.attributes("id").toString())(
        valueNode =>
          External
            .create(
              NonEmptyString.create(valueNode.attributes("id").toString()).get,
              NonEmptyString
                .create(valueNode.attributes("name").toString())
                .get,
              (valueNode \ "availability")
                .map(
                  childNode =>
                    Availability
                      .create(
                        Period
                          .create(
                            LocalDateTime
                              .parse(childNode.attributes("start").toString()),
                            LocalDateTime
                              .parse(childNode.attributes("end").toString())
                          )
                          .get,
                        Preference
                          .create(
                            childNode.attributes("preference").toString().toInt
                          )
                          .get
                      )
                      .get
                )
                .toList,
              roles = mappedRoles
                .getOrElse(valueNode.attributes("id").toString(), List.empty)
                .toList
          )
      )
      .toList
    // TODO: There may exist teachers or externals that are not on vivas and the mapped results will indicate it as None
    // However None can also be indicated by an illegal state of a domain class which causes conflict to verify if the
    // domain is valid

    println(mappedExternals)

    //

    println(vivasXML)
    val xxx = vivasXML \ "president"
    print(xxx)
    val vivas = vivasXML
      .map(
        node =>
          Viva
            .create(
              NonEmptyString.create(node.attributes("student").toString()).get,
              NonEmptyString.create(node.attributes("title").toString()).get,
              Jury
                .create(
                  president = mappedTeachers
                    .find(
                      p =>
                        p._2.head.get.id == (node \ "president")
                          .map(
                            childNode =>
                              NonEmptyString
                                .create(childNode.attributes("id").toString())
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
                                .create(childNode.attributes("id").toString())
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
                                .create(childNode.attributes("id").toString())
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
                                .create(childNode.attributes("id").toString())
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
