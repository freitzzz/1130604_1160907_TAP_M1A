package assessment

import java.io.File

import io.FileIO._
import org.scalatest.funsuite.AnyFunSuite
import software.purpledragon.xml.compare.XmlCompare

import scala.util.{Failure, Success, Try}
import scala.xml.Elem

trait AssessmentBehaviours {
  this: AnyFunSuite =>

  val IN = "_in.xml" // Input file termination
  val OUT = "_out.xml" // Output file termination
  val OUTERROR = "_outerror.xml" // Error file termination
  def PATH: String // Assessment file path

  private def testXml(eoxml: Elem, oxml: Elem): Try[Boolean] = {
    val cmp = XmlCompare.compare(eoxml, oxml)
    if (cmp.isEqual) Success(true) else Failure(new Exception(cmp.message))
  }

  private def testWithFailure(t: Throwable, f: File): (File, Try[Boolean]) = {
    val efn = f.getPath.replace(IN, OUTERROR)
    val fileError = loadError(efn).getMessage
    val test =
      if (t.getMessage.equals(fileError)) Success(true)
      else
        Failure(
          new Exception(
            s"Created failure: ${t.getMessage} did not correspond to $fileError"
          )
        )
    (f, test)
  }

  private def testWithSuccess(xml: Elem, f: File): (File, Try[Boolean]) = {
    val ofn = f.getPath.replace(IN, OUT)

    val test = for {
      eoxml <- load(ofn) // load expected output file
      result <- testXml(eoxml, xml) // compare outputs expected vs real
    } yield result
    (f, test)
  }

  private def testFile(f: File, ms: Elem => Try[Elem]): (File, Try[Boolean]) = {

    if (f.getPath.endsWith("valid_agenda_control_27_in.xml")) {
      println("a")
    }

    val tout = for {
      ixml <- load(f) // load input file
      oxml <- ms(ixml) // convert input file into output file
    } yield oxml
    tout.fold(t => testWithFailure(t, f), xml => testWithSuccess(xml, f))
  }

  def performTests(ms: Elem => Try[Elem]): Unit = {
    test("Assessment of Milestone " + ms.getClass.getSimpleName) {
      val testInputFiles = new File(PATH).listFiles(_.getName.endsWith(IN))
      val numTests = testInputFiles.size
      val tested = testInputFiles.map(testFile(_, ms))
      tested.foreach {
        case (f, t) => println(s"File: ${f.getName} Result: $t")
      }
      val passedTests = tested.filter { case (_, t) => t.isSuccess }.length
      val ratio: Int = (passedTests * 100) / numTests
      println(s"Final score: $passedTests / ${testInputFiles.size} = $ratio")
      assert(passedTests == numTests)
    }
  }
}
