package assessment

import assessment.AssessmentMS01.create
import org.scalatest.funsuite.AnyFunSuite

class AssessmentMS01Test extends AnyFunSuite with AssessmentBehaviours {

  val PATH = "files/assessment/ms01" // Assessment file path

  performTests(create)
}
