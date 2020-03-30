package io

import java.io.{File, PrintWriter}

import scala.util.Try
import scala.xml.{Elem, PrettyPrinter, XML}

object FileIO {

  // load xml by filename
  def load(fname: String): Try[Elem] = {
    Try(XML.loadFile(fname))
  }

  // load xml by java.io.File
  def load(f: File): Try[Elem] = {
    Try(XML.loadFile(f))
  }

  // load xml error by filename
  def loadError(fname: String): Throwable = {
    Try(XML.loadFile(fname)).fold( t => t , xml => {
      val errorMessage  = xml \@ "message"
      if (errorMessage.isEmpty) new Exception(s"There is no error message in file $fname")
      else new Exception(errorMessage)
    })
  }

  // save xml to filename
  private val normalizer = new PrettyPrinter(120, 4)
  def save(fname: String, xml: Elem): Unit = {

    val prettyXml = normalizer.format(xml)
    new PrintWriter(fname) { println("<?xml version='1.0' encoding=\"UTF-8\"?>"); println(prettyXml); close() }
  }

}
