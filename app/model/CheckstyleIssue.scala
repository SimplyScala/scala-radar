package model

import scala.xml.Elem
import model.CheckstyleIssue.Severity

object CheckstyleIssue {
    type CheckStyleReport = Elem
    type Severity = String
}

// TODO use java.nio.file.Path for fromPath param
// TODO def className (from path)
case class CheckstyleIssue(severity: String, message: String, line: Option[Int], fromPath: String)