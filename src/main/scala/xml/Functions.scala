package xml

import java.io.ByteArrayInputStream
import java.time
import java.time.{LocalDateTime, LocalTime}
import java.time.format.DateTimeFormatter

import domain.model.{
  Adviser,
  Agenda,
  Availability,
  CoAdviser,
  Duration,
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

    val vivasDurationTry = Duration.create(
      time.Duration.between(
        LocalTime.ofNanoOfDay(0),
        LocalTime
          .parse(elem \@ "duration", DateTimeFormatter.ISO_LOCAL_TIME),
      )
    )

    vivasDurationTry match {
      case Failure(exception)     => Failure(exception)
      case Success(vivasDuration) =>
        // Retrieve Vivas
        val vivasXML = elem \ "vivas" \ "viva"

        val mappedRoles = vivasXML
          .map(
            viva =>
              viva.descendant.filter(node => node.attribute("id").nonEmpty)
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
          .map(
            entry => (entry._1, entry._2.map(tuple => tuple._2).distinct.toList)
          )

        // Retrieve teachers
        val teachersXML = elem \ "resources" \ "teachers" \ "teacher"

        val externalsXML = elem \ "resources" \ "externals" \ "external"

        val teachersProperties =
          mapResourcesProperties(teachersXML, mappedRoles)

        val externalsProperties =
          mapResourcesProperties(externalsXML, mappedRoles)

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

            val vivasPropertiesFailures = vivasProperties
              .flatMap(ps => List(ps._1, ps._2, ps._3, ps._4))
              .find(_.isFailure)

            vivasPropertiesFailures match {
              case Some(value) => Failure(value.failed.get)
              case None =>
                val vivas = vivasProperties.map(
                  properties =>
                    Viva
                      .create(
                        properties._1.get,
                        properties._2.get,
                        Jury
                          .create(
                            properties._3.get,
                            properties._4.get,
                            properties._5.toList,
                            properties._6.toList
                          )
                          .get,
                        vivasDuration
                    )
                )

                Success(vivas.toList)
            }

          } else {
            Failure(firstInvalidResource.get.failed.get)
          }

        } else {
          Failure(firstInvalidProperty.get.failed.get)
        }
    }

  }

  def serialize(agenda: Agenda): Elem = {

    val xml =
      <schedule xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" totalPreference={agenda.scheduledVivas.foldLeft(0)(_ + _.scheduledPreference).toString}>
        {agenda.scheduledVivas.map(serializeScheduledViva)}
      </schedule>

    xml

  }

  def serializeError(error: Throwable): Elem = {
    val errorXML =
      <error xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" message={error.getMessage}  />
    errorXML
  }

  private def serializeScheduledViva(scheduledViva: ScheduledViva): Node = {

    val juryXML = serializeJury(scheduledViva.viva.jury)

    val xml =
      <viva student={scheduledViva.viva.student.s} title={scheduledViva.viva.title.s} start={scheduledViva.period.start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)} end={scheduledViva.period.end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)} preference={scheduledViva.scheduledPreference.toString}>
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
     Try[Resource],
     Try[Resource],
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
            .headOption
            .fold[Try[Resource]](
              Failure(
                new IllegalStateException(
                  "Node president is empty/undefined in viva"
                )
              )
            )(p => Success(p.get._2)),
          (node \ "adviser")
            .map(p => resources.find(_._1 == p \@ "id"))
            .headOption
            .fold[Try[Resource]](
              Failure(
                new IllegalStateException(
                  "Node adviser is empty/undefined in viva"
                )
              )
            )(p => Success(p.get._2)),
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
