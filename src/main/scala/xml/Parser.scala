package xml

import java.time.format.DateTimeFormatter
import java.time.{Duration, LocalDateTime, LocalTime}

import domain.model.{
  Adviser,
  Availability,
  CoAdviser,
  External,
  Jury,
  NonEmptyString,
  Period,
  Preference,
  President,
  Resource,
  Role,
  Supervisor,
  Teacher,
  Viva
}

import scala.util.{Failure, Success, Try}
import scala.xml.{Elem, NodeSeq}

object Parser {

  def parse(elem: Elem): Try[List[Viva]] = {

    val vivasDuration = Duration.between(
      LocalTime.ofNanoOfDay(0),
      LocalTime
        .parse(elem \@ "duration", DateTimeFormatter.ISO_LOCAL_TIME),
    )

    // Retrieve Vivas
    val vivasXML = elem \ "vivas" \ "viva"

    val mappedRoles = vivasXML
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
      .groupBy(role => role._1)
      .map(entry => (entry._1, entry._2.map(tuple => tuple._2).distinct.toList))

    // Retrieve teachers
    val teachersXML = elem \ "resources" \ "teachers" \ "teacher"

    val externalsXML = elem \ "resources" \ "externals" \ "external"

    val teachersProperties = mapResourcesProperties(teachersXML, mappedRoles)

    val externalsProperties = mapResourcesProperties(externalsXML, mappedRoles)

    val resourcesProperties = teachersProperties ++ externalsProperties

    val firstInvalidProperty = resourcesProperties.values
      .flatMap[Try[Any]](
        tuple =>
          List(tuple._1, tuple._2) ++ tuple._3
            .flatMap(innerTuple => List(innerTuple._1, innerTuple._2))
      )
      .find(_.isFailure)

    if (firstInvalidProperty.isEmpty) {

      val teachers = teachersProperties
        .map(
          tuple =>
            (
              tuple._1,
              Teacher.create(
                tuple._2._1.get,
                tuple._2._2.get,
                tuple._2._3
                  .map(
                    availabilityTuple =>
                      Availability
                        .create(
                          availabilityTuple._1.get,
                          availabilityTuple._2.get
                        )
                        .get
                  )
                  .toList,
                tuple._2._4
              )
          )
        )

      val externals = externalsProperties
        .map(
          tuple =>
            (
              tuple._1,
              External.create(
                tuple._2._1.get,
                tuple._2._2.get,
                tuple._2._3
                  .map(
                    availabilityTuple =>
                      Availability
                        .create(
                          availabilityTuple._1.get,
                          availabilityTuple._2.get
                        )
                        .get
                  )
                  .toList,
                tuple._2._4
              )
          )
        )

      val resources = teachers ++ externals

      val firstInvalidResource = resources.values.find(_.isFailure)

      if (firstInvalidResource.isEmpty) {

        val resourcess = resources.map(tuple => (tuple._1, tuple._2.get))

        val vivasProperties = mapVivasProperties(vivasXML, resourcess)

        val firstInvalidVivaProperty = vivasProperties
          .flatMap(
            properties =>
              List(
                properties._1,
                properties._2,
                Jury
                  .create(
                    properties._3,
                    properties._4,
                    properties._5.toList,
                    properties._6.toList
                  )
            )
          )
          .find(_.isFailure)

        if (firstInvalidVivaProperty.isEmpty) {

          val vivas = vivasProperties.map(
            properties =>
              Viva
                .create(
                  properties._1.get,
                  properties._2.get,
                  Jury
                    .create(
                      properties._3,
                      properties._4,
                      properties._5.toList,
                      properties._6.toList
                    )
                    .get,
                  vivasDuration
                )
                .get
          )

          Success(vivas.toList)

        } else {

          Failure(firstInvalidVivaProperty.get.failed.get)

        }

      } else {
        Failure(firstInvalidResource.get.failed.get)
      }

    } else {
      Failure(firstInvalidProperty.get.failed.get)
    }

  }

  private def mapResourcesProperties(
    nodeSeq: NodeSeq,
    roles: Map[String, List[Role]]
  ): Map[String,
         (Try[NonEmptyString],
          Try[NonEmptyString],
          Seq[(Try[Period], Try[Preference])],
          List[Role])] = {

    nodeSeq
      .filter(node => roles.contains(node \@ "id"))
      .map(
        node =>
          (
            node \@ "id",
            (
              NonEmptyString.create(node \@ "id"),
              NonEmptyString.create(node \@ "name"),
              (node \ "availability").map(
                childNode =>
                  (
                    Period
                      .create(
                        LocalDateTime
                          .parse(childNode \@ "start"),
                        LocalDateTime
                          .parse(childNode \@ "end")
                      ),
                    Preference
                      .create((childNode \@ "preference").toInt)
                )
              ),
              roles(node \@ "id")
            )
        )
      )
      .toMap
  }

  private def mapVivasProperties(nodeSeq: NodeSeq,
                                 resources: Map[String, Resource]): Seq[
    (Try[NonEmptyString],
     Try[NonEmptyString],
     Resource,
     Resource,
     Seq[Resource],
     Seq[Resource])
  ] = {

    nodeSeq.map(
      node =>
        (
          NonEmptyString.create(node \@ "student"),
          NonEmptyString.create(node \@ "title"),
          (node \ "president")
            .map(p => resources.find(_._1 == p \@ "id"))
            .head
            .get
            ._2,
          (node \ "adviser")
            .map(p => resources.find(_._1 == p \@ "id"))
            .head
            .get
            ._2,
          (node \ "supervisor")
            .map(p => resources.find(_._1 == p \@ "id"))
            .map(_.get._2),
          (node \ "coadviser")
            .map(p => resources.find(_._1 == p \@ "id"))
            .map(_.get._2)
      )
    )

  }

}
