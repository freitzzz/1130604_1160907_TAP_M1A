package assessment

import org.scalatest.funsuite.AnyFunSuite
import assessment.AssessmentMS03.create

class AssessmentMS03Test extends AnyFunSuite with AssessmentBehaviours {

  val PATH = "files/assessment/ms03" // Assessment file path

  performTests(create)
}
