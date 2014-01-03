package service.engine

import org.scalatest.{Matchers, FunSuite}
import scala.xml.XML
import service.engine.CheckstyleXMLParser
import model.CheckstyleIssue

class CheckstyleXMLParserTest extends FunSuite with Matchers {
    val pathFile = "/somepath/src/main/scala/some/package/ResourceReader.scala"
    val line = 1
    val severity = "warning"
    val message = "some message"

    test("produce CheckstyleError from checkstyle xml element") {
        val checkstyle_xml_pattern =
            s"""<?xml version="1.0" encoding="UTF-8"?>
                   <checkstyle version="5.0">
                       <file name="$pathFile">
                           <error line="$line" source="" severity="$severity" message="$message"></error>
                       </file>
                   </checkstyle>"""

        val checkstyleReport = XML.loadString(checkstyle_xml_pattern)

        CheckstyleXMLParser.produceIssues(checkstyleReport) should be (List(CheckstyleIssue(severity, message, Option(line), pathFile)))
    }

    test("with optional param, should produce CheckStyleError from checkstyle xml element") {
        val checkstyle_xml_pattern_with_no_file_attr =
            s"""<?xml version="1.0" encoding="UTF-8"?>
                   <checkstyle version="5.0">
                       <file name="$pathFile">
                           <error source="" severity="$severity" message="$message"></error>
                       </file>
                   </checkstyle>"""

        val checkstyleReport = XML.loadString(checkstyle_xml_pattern_with_no_file_attr)

        CheckstyleXMLParser.produceIssues(checkstyleReport) should be (List(CheckstyleIssue(severity, message, None, pathFile)))
    }

    // TODO test avec scalacheck ?
}