package model.engine

import scala.xml.Elem

object CheckstyleIssue {
    type CheckStyleReport = Elem
    type Severity = String
}

// TODO use scalax.file.Path for fromPath param
// TODO def className (from path)
case class CheckstyleIssue(severity: String, message: String, line: Option[Int], fromPath: String)