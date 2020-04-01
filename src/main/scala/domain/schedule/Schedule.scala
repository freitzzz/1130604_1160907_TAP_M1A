package domain.schedule

import scala.util.Try
import scala.xml.Elem

trait Schedule {
  /** Schedule an agenda from xml element
   *
   * @param xml agenda in xml format
   * @return a complete schedule in xml format or an error
   */
  def create(xml: Elem): Try[Elem]
}
