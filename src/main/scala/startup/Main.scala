package startup

import domain.model._
import io.FileIO
import java.time.LocalDateTime

import scala.xml.XML

object Main {
  def main(args: Array[String]): Unit = {

    println("Please insert the name of the file to read: ")

    //val inputFile = scala.io.StdIn.readLine()

    println("Please insert the name you want for the output file: ")
    //val outputFileName = scala.io.StdIn.readLine()

    val fileLoader = FileIO.load(
      "/home/freitas/Development/Projects/TAP/1130604_1160907_tap_m1a/files/assessment/ms01/valid_agenda_in.xml"
    )

    val agenda = fileLoader.get

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
      .map(roleTuple => (roleTuple._1, roleTuple._2.map(tuple => tuple._2.get)))

    println(mappedRoles)

    // Retrieve teachers
    val teachersXML = agenda \ "resources" \ "teachers" \ "teacher"
    val mappedTeachers = teachersXML
      .groupMap(keyNode => keyNode.attributes("id").toString())(
        valueNode =>
          Teacher
            .create(
              valueNode.attributes("id").toString(),
              valueNode.attributes("name").toString(),
              (valueNode \ "availability")
                .map(
                  childNode =>
                    Availability
                      .create(
                        LocalDateTime
                          .parse(childNode.attributes("start").toString()),
                        LocalDateTime
                          .parse(childNode.attributes("end").toString()),
                        childNode.attributes("preference").toString().toInt
                      )
                      .get
                )
                .toList,
              roles = mappedRoles(valueNode.attributes("id").toString()).toList
          )
      )

    println(mappedTeachers)

    // Retrieve externals
    val externalsXML = agenda \ "resources" \ "externals" \ "external"

    val mappedExternals = externalsXML
      .groupMap(keyNode => keyNode.attributes("id").toString())(
        valueNode =>
          External
            .create(
              valueNode.attributes("id").toString(),
              valueNode.attributes("name").toString(),
              (valueNode \ "availability")
                .map(
                  childNode =>
                    Availability
                      .create(
                        LocalDateTime
                          .parse(childNode.attributes("start").toString()),
                        LocalDateTime
                          .parse(childNode.attributes("end").toString()),
                        childNode.attributes("preference").toString().toInt
                      )
                      .get
                )
                .toList,
              roles = mappedRoles(valueNode.attributes("id").toString()).toList
          )
      )

    println(mappedExternals)

    //

    println(vivasXML)
    val xxx = vivasXML \ "president"
    print(xxx)
//    val vivas = vivasXML
//      .map(
//        node =>
//          Viva
//            .create(
//              node.attributes("student").toString(),
//              node.attributes("title").toString(),
//              Jury
//                .create(
//                  president = mappedTeachers
//                    .find(
//                      p =>
//                        p.id == (node \ "president")
//                          .map(
//                            childNode => childNode.attributes("id").toString()
//                          )
//                          .head
//                    )
//                    .get,
//                  adviser = mappedTeachers
//                    .find(
//                      a =>
//                        a.id == (node \ "adviser")
//                          .map(
//                            childNode => childNode.attributes("id").toString()
//                          )
//                          .head
//                    )
//                    .get,
//                  supervisors = (node \ "supervisor")
//                    .map(
//                      childNode =>
//                        mappedExternals
//                          .find(
//                            s => s.id == childNode.attributes("id").toString()
//                          )
//                          .get
//                    )
//                    .toList,
//                  coAdvisers = (node \ "coadviser")
//                    .map(
//                      childNode =>
//                        mappedExternals
//                          .find(
//                            s => s.id == childNode.attributes("id").toString()
//                          )
//                          .get
//                    )
//                    .toList
//                )
//                .get
//            )
//            .get
//      )
//      .toList

    val x = 2

  }
}
