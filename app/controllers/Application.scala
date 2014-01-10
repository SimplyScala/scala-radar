package controllers

import play.api._
import play.api.mvc._
import service.engine.{ScalaProjectParser, CoberturaXMLParser, CheckstyleXMLParser}
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.json.Writes._
import scalax.file.Path

object Application extends Controller {

    /*implicit val issueWriter = Json.writes[CheckstyleIssue]
    implicit val issuesWriter = Writes.seq(issueWriter)*/

    def index = Action {
        Ok(views.html.index())
    }

    // TODO récupérer le checkstyle non stubber
    // TODO récupérer le cobertura non stubber
    def project(projectName: String) = Action {
        import scala.xml.XML.loadFile
        import scalax.file.ImplicitConversions._

        val coberturaReport = CoberturaXMLParser.produceCodeCoverageReport(loadFile("public/resources/coverage-report/cobertura.xml"))
        val checkstyleIssues = CheckstyleXMLParser.produceIssues(loadFile("public/resources/scala-radar_style.xml"))

        val sourcePath = "/Users/ugobourdon/Dev/Projects/ScalaQuality/scala-radar/app"
        val scalaFiles: Set[Path] = (sourcePath ** "*.scala").toSet

        val projectMetadatas = ScalaProjectParser.produceScalaProjectMetadatas(scalaFiles)

        Ok (views.html.project(projectName, coberturaReport, checkstyleIssues, projectMetadatas))
    }

    // TODO récupérer le checkstyle non stubber
    // TODO récupérer le cobertura non stubber
    def issues(projectName: String) = Action { implicit request =>
        val report = scala.xml.XML.loadFile("public/resources/scala-radar_style.xml")
        val checkstyleIssues = CheckstyleXMLParser.produceIssues(report)

        val groupedMessages = checkstyleIssues
            .groupBy(x => x.message)
            .values
            .map( seq => (seq.head.message, seq.size) )
            .toSeq

        /*render {
            case Accepts.Html() => Ok(views.html.issues(checkstyleIssues, groupedMessages))
            case Accepts.Json() => Ok(Json.toJson(checkstyleIssues))
        }*/
        Ok(views.html.issues(projectName, checkstyleIssues, groupedMessages))
    }

    def coverage(projectName: String) = Action {
        //val indexFile = io.Source.fromFile("/assets/resources/scctReport/index.html").mkString
        //val result = io.Source.fromFile("public/resources/scctReport/index.html").mkString
        //val result = Assets.Found("/assets/resources/scctReport/index.html").toString()

        //Ok(views.html.coverage(projectName, Html(result)))

        Assets.Found("/assets/resources/coverage-report/index.html")
    }
}