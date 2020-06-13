package assessment

import assessment.AssessmentMS03.create
import org.scalatest.funsuite.AnyFunSuite

class AssessmentMS03Test extends AnyFunSuite with AssessmentBehaviours {

  val PATH = "files/assessment/ms03" // Assessment file path

  performTests(create)
}
