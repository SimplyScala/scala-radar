package model

import scala.xml.Elem

object CoberturaReport {
    type CoberturaXMLReport = Elem
}

case class CoberturaReport(lineRate: Double)