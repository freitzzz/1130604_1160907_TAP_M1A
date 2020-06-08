package assessment

import org.scalatest.funsuite.AnyFunSuite
import assessment.AssessmentMS01.create

class AssessmentMS01Test extends AnyFunSuite with AssessmentBehaviours {

  val PATH = "files/assessment/ms01" // Assessment file path

  performTests(create)
}
