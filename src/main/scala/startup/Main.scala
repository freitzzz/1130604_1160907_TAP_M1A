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
      "C:\\Tap\\1130604_1160907_tap_m1a\\files\\assessment\\ms01\\valid_agenda_in.xml"
    )

    val agenda = fileLoader.get

    // Retrieve teachers
    val teachersXML = agenda \ "resources" \ "teachers" \ "teacher"
    val mappedTeachers = teachersXML
      .map(
        node =>
          Teacher(
            node.attributes("id").toString(),
            node.attributes("name").toString(),
            (node \ "availability")
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
            roles = List[Role]()
        )
      )
      .toList

    // Retrieve externals
    val externalsXML = agenda \ "resources" \ "externals" \ "external"
    val mappedExternals = externalsXML
      .map(
        node =>
          Teacher(
            node.attributes("id").toString(),
            node.attributes("name").toString(),
            (node \ "availability")
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
            roles = List[Role]()
        )
      )
      .toList

    // Retrieve Vivas
    val vivasXML = agenda \ "vivas" \ "viva"
    println(vivasXML)
    val xxx = vivasXML \ "president"
    print(xxx)
    val vivas = vivasXML
      .map(
        node =>
          Viva(
            node.attributes("student").toString(),
            node.attributes("title").toString(),
            jury = Jury
              .create(
                president = mappedTeachers
                  .find(
                    p =>
                      p.id == (node \ "president")
                        .map(childNode => childNode.attributes("id").toString())
                        .head
                  )
                  .get,
                adviser = mappedTeachers
                  .find(
                    a =>
                      a.id == (node \ "adviser")
                        .map(childNode => childNode.attributes("id").toString())
                        .head
                  )
                  .get,
                supervisors = (node \ "supervisor")
                  .map(
                    childNode =>
                      mappedExternals
                        .find(
                          s => s.id == childNode.attributes("id").toString()
                        )
                        .get
                  )
                  .toList,
                coAdvisers = (node \ "coadviser")
                  .map(
                    childNode =>
                      mappedExternals
                        .find(
                          s => s.id == childNode.attributes("id").toString()
                        )
                        .get
                  )
                  .toList
              )
              .get
        )
      )
      .toList

    val x = 2

  }
}
