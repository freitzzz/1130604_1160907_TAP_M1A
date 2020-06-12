package assessment

import org.scalatest.funsuite.AnyFunSuite
import assessment.CustomAssessmentMS03.create

class CustomAssessmentMS03Test extends AnyFunSuite with AssessmentBehaviours {

  val PATH = "files/assessment/customAssessmentMs03" // Assessment file path

  performTests(create)
}
