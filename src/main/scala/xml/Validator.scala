package xml

import java.io.ByteArrayInputStream

import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory

import scala.util.Try
import scala.xml.Elem

object Validator {

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

}
