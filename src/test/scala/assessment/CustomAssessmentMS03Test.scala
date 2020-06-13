package assessment

import assessment.CustomAssessmentMS03.create
import org.scalatest.funsuite.AnyFunSuite

class CustomAssessmentMS03Test extends AnyFunSuite with AssessmentBehaviours {

  val PATH = "files/assessment/customAssessmentMs03" // Assessment file path

  performTests(create)
}
