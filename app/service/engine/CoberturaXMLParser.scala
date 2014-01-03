package service.engine

import model.CoberturaReport
import model.CoberturaReport.CoberturaXMLReport

object CoberturaXMLParser {
    def produceCodeCoverageReport(coberturaReport: CoberturaXMLReport): CoberturaReport = {
        CoberturaReport( ( coberturaReport \ "@line-rate" ).text.toDouble * 100 )
    }
}