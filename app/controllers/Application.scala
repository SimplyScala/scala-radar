package controllers

import play.api._
import play.api.mvc._
import service.engine.{ScalaProjectParser, CoberturaXMLParser, CheckstyleXMLParser}
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.json.Writes._
import scalax.file.Path
import dao.{ProdDatabase, SlickDao}
import scala.xml.XML._

object Application extends Controller with ProdDatabase {

    /*implicit val issueWriter = Json.writes[CheckstyleIssue]
    implicit val issuesWriter = Writes.seq(issueWriter)*/

    def index = Action {
        Ok(views.html.index())
    }

    def project(projectName: String) = Action {
        import scala.xml.XML.loadFile
        import scalax.file.ImplicitConversions._

        val mayBeLastBuild = SlickDao.retrieveLastBuild(projectName)

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

    def issues(projectName: String) = Action { implicit request =>

        val mayBeLastBuild = SlickDao.retrieveLastBuild(projectName)

        mayBeLastBuild
            .map {  build =>
                val checkStyleFilePath = s"${build.projectPath}/target/scalastyle-report/${projectName}_report.xml"

                val checkstyleIssues = CheckstyleXMLParser.produceIssues(loadFile(checkStyleFilePath))
                val groupedMessages = checkstyleIssues
                    .groupBy(x => x.message)
                    .values
                    .map( seq => (seq.head.message, seq.size) )
                    .toSeq

                Ok(views.html.issues(projectName, checkstyleIssues, groupedMessages))
            }
            .getOrElse { NotFound(s"project $projectName not found !") }
    }

    def coverage(projectName: String) = Action {
        SlickDao.retrieveLastBuild(projectName) map {    build =>
            val coberturaReportFileUrl = s"/public/build/${build.projectName}/${build.buildId}/target/scala-2.10/coverage-report/index.html"
            Ok(coberturaReportFileUrl)
        } getOrElse { NotFound(s"project $projectName not found !") }
    }
}