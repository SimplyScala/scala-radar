package service.engine

import org.scalatest.{Matchers, FunSuite}
import model.engine.CoberturaReport

class CoberturaXMLParserTest extends FunSuite with Matchers {
    test("retrieve global code coverage") {  // TODO use string xml instead of file in public/resources
        val report = scala.xml.XML.loadFile("public/resources/coverage-report/cobertura.xml")
        CoberturaXMLParser.produceCodeCoverageReport(report) should be (CoberturaReport(52.0))
    }
}