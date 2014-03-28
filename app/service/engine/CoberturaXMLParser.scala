package service.engine

import model.engine.CoberturaReport
import CoberturaReport.CoberturaXMLReport
import model.engine.CoberturaReport

object CoberturaXMLParser {
    def produceCodeCoverageReport(coberturaReport: CoberturaXMLReport): CoberturaReport = {
        CoberturaReport( ( coberturaReport \ "@line-rate" ).text.toDouble * 100 )
    }
}