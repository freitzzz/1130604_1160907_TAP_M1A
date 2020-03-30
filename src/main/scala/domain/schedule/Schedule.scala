package domain.schedule

import scala.util.Try
import scala.xml.Elem

trait Schedule {
  def create(xml: Elem): Try[Elem]
}
