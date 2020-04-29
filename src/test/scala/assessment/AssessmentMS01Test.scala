package assessment

import org.scalatest.funsuite.AnyFunSuite
import assessment.AssessmentMS01.create
import io.FileIO
import xml.Functions

import scala.util.{Failure, Success}

class AssessmentMS01Test extends AnyFunSuite with AssessmentBehaviours {

  val PATH = "files/assessment/ms01" // Assessment file path

  performTests(create)
}
