package xml

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import scala.xml.XML

class FunctionsTest extends AnyFunSuite with Matchers {

  test(
    "given an XML document that is not valid according to a XML schema, validation fails"
  ) {

    // Arrange

    val xml =
      XML.loadString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<band/>")

    val schema = XML.loadString(
      "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<!-- Created with Liquid Technologies Online Tools 1.0 (https://www.liquid-technologies.com) -->\n<xs:schema attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n  <xs:element name=\"band\">\n    <xs:complexType>\n      <xs:attribute name=\"name\" type=\"xs:string\" use=\"required\" />\n    </xs:complexType>\n  </xs:element>\n</xs:schema>"
    )

    // Act

    val validation = Functions.validate(xml, schema)

    // Arrange

    validation.isFailure shouldBe true

  }

  test(
    "given an XML document that is valid according to a XML schema, validation succeeds"
  ) {

    // Arrange

    val xml =
      XML.loadString(
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<band name=\"100gecs\" />"
      )

    val schema = XML.loadString(
      "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<!-- Created with Liquid Technologies Online Tools 1.0 (https://www.liquid-technologies.com) -->\n<xs:schema attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n  <xs:element name=\"band\">\n    <xs:complexType>\n      <xs:attribute name=\"name\" type=\"xs:string\" use=\"required\" />\n    </xs:complexType>\n  </xs:element>\n</xs:schema>"
    )

    // Act

    val validation = Functions.validate(xml, schema)

    // Arrange

    validation.isSuccess shouldBe true

  }
}
