package controllers

import play.api._
import play.api.mvc._
import service.engine.{ScalaProjectParser, CoberturaXMLParser, CheckstyleXMLParser}
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.json.Writes._
import scalax.file.Path
import dao.{ProdDatabase, Dao}
import model.SuccessfulBuild
import sorm.Persisted

object Application extends Controller with ProdDatabase {

    /*implicit val issueWriter = Json.writes[CheckstyleIssue]
    implicit val issuesWriter = Writes.seq(issueWriter)*/

    def index = Action {
        Ok(views.html.index())
    }

    def project(projectName: String) = Action {          // TODO in fact => last project() uri
        import scala.xml.XML.loadFile
        import scalax.file.ImplicitConversions._

        val mayBeLastBuild = Dao.retrieveLastBuild()

        mayBeLastBuild
            .map { build =>
                val projectPath = build.projectPath

                val coberturaReportFilePath = s"$projectPath/target/scala-2.10/coverage-report/cobertura.xml"
                val checkStyleFilePath = s"$projectPath/target/scalastyle-report/${projectName}_report.xml"
                val scalaFiles: Set[Path] = (s"$projectPath/app" ** "*.scala").toSet

                val coberturaReport = CoberturaXMLParser.produceCodeCoverageReport(loadFile(coberturaReportFilePath))
                val checkstyleIssues = CheckstyleXMLParser.produceIssues(loadFile(checkStyleFilePath))
                val projectMetadatas = ScalaProjectParser.produceScalaProjectMetadatas(scalaFiles)

                Ok(views.html.project(projectName, coberturaReport, checkstyleIssues, projectMetadatas))
            }
            .getOrElse { NotFound(s"project $projectName not found !") }
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

    // NOT USED
    def coverage(projectName: String) = Action {
        //val indexFile = io.Source.fromFile("/assets/resources/scctReport/index.html").mkString
        //val result = io.Source.fromFile("public/resources/scctReport/index.html").mkString
        //val result = Assets.Found("/assets/resources/scctReport/index.html").toString()

        //Ok(views.html.coverage(projectName, Html(result)))
        val mayBeLastBuild = Dao.retrieveLastBuild()

        //Ok(mayBeLastBuild.get.buildId + mayBeLastBuild.get.projectPath)
        Assets.Found("/assets/resources/coverage-report/index.html")
    }
}