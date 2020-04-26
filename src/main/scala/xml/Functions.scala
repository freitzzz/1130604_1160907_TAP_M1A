package xml

import java.io.ByteArrayInputStream
import java.time.format.DateTimeFormatter
import java.time.{Duration, LocalDateTime, LocalTime}

import domain.model.{
  Adviser,
  Agenda,
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
  ScheduledViva,
  Supervisor,
  Teacher,
  Viva
}
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory

import scala.util.{Failure, Success, Try}
import scala.xml.{Elem, Node, NodeSeq}

object Functions {

  /**
    * A no-side effect XML schema validation
    * Returns a Try indicating whether the XML document passed by parameter is valid by a given XML Schema given by parameter
    * Credits for this solution goes to:
    *   - https://github.com/scala/scala-xml/wiki/XML-validation
    *   - https://gist.github.com/ramn/725139/7b9f510adf385ece8f7c37e361c8ea4862def382
    *   - https://gist.github.com/ramn/725139/7b9f510adf385ece8f7c37e361c8ea4862def382#gistcomment-991893
    */
  def validate(xml: Elem, schema: Elem): Try[Unit] = {

    Try({
      val schemaLang = javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI
      val factory = SchemaFactory.newInstance(schemaLang)
      val schemaSAX =
        factory.newSchema(
          new StreamSource(new ByteArrayInputStream(schema.toString.getBytes))
        )
      val validator = schemaSAX.newValidator()
      validator.validate(
        new StreamSource(new ByteArrayInputStream(xml.toString.getBytes))
      )
    })

  }

  def deserialize(elem: Elem): Try[List[Viva]] = {

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

  def serialize(agenda: Agenda): Elem = {

    val xml =
      <schedule totalPreference={agenda.scheduledVivas.foldLeft(0)(_ + _.scheduledPreference).toString}>
        {agenda.scheduledVivas.map(serializeScheduledViva)}
      </schedule>

    xml

  }

  private def serializeScheduledViva(scheduledViva: ScheduledViva): Node = {

    val juryXML = serializeJury(scheduledViva.viva.jury)

    val xml =
      <viva student={scheduledViva.viva.student.s} title={scheduledViva.viva.title.s} start={scheduledViva.period.start.toString} end={scheduledViva.period.end.toString} preference={scheduledViva.scheduledPreference.toString}>
        {juryXML}
      </viva>

    xml

  }

  private def serializeJury(jury: Jury): List[Node] = {

    val presidentXML = <president name={jury.president.name.s}/>

    val adviserXML = <adviser name={jury.adviser.name.s}/>

    val supervisorsXML =
      jury.supervisors.map(supervisor => <supervisor name={supervisor.name.s}/>)

    val coAdvisersXML =
      jury.coAdvisers.map(coAdviser => <coadviser name={coAdviser.name.s}/>)

    List(presidentXML, adviserXML, supervisorsXML, coAdvisersXML).flatten

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
