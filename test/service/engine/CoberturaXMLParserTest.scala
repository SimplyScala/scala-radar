package service.engine

import org.scalatest.{Matchers, FunSuite}
import model.CoberturaReport

class CoberturaXMLParserTest extends FunSuite with Matchers {
    test("retrieve global code coverage") {
        val report = scala.xml.XML.loadFile("public/resources/coverage-report/cobertura.xml")
        CoberturaXMLParser.produceCodeCoverageReport(report) should be (CoberturaReport(96.0))
    }
}