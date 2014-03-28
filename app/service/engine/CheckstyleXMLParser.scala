package service.engine

import scala.xml.{NodeSeq, Node}
import model.engine.CheckstyleIssue
import CheckstyleIssue.CheckStyleReport

object CheckstyleXMLParser {
    def produceIssues(checkstyleReport: CheckStyleReport): Seq[CheckstyleIssue] = {
        for {
            file <- (checkstyleReport \ "file")
            error <- extractChildNodes(file)
        } yield {
            CheckstyleIssue(extractAttributValue(error, "severity"),
                extractAttributValue(error, "message"),
                extractOptionalAttributValue(error, "line").map(_.toInt),
                extractAttributValue(file, "name"))
        }
    }

    private def extractChildNodes(parentNode: Node): NodeSeq = (parentNode \ "error")
    private def extractAttributValue(node: Node, attributName: String): String = (node \ s"@$attributName").text
    private def extractOptionalAttributValue(node: Node, attributName: String): Option[String] = {
        val attr = (node \ s"@$attributName")
        if(attr.isEmpty) None else Option(attr.text)
    }
}