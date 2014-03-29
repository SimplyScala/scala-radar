package dao.schema

import scala.slick.driver.HsqldbDriver.simple._
import model.SuccessfulBuild

class BuildSchema(tag: Tag) extends Table[SuccessfulBuild](tag, "SUCCESSFUL_BUILDS") {
    def buildId = column[String]("BUILD_ID", O.PrimaryKey)
    def startDate = column[Long]("START_DATE")
    def endDate = column[Long]("END_DATE")
    def path = column[String]("PATH")
    def projectName = column[String]("PROJECT_NAME")
    def projectUrl = column[String]("PROJECT_URL")

    def * = (buildId, startDate, endDate, path, projectName, projectUrl) <> (SuccessfulBuild.tupled, SuccessfulBuild.unapply)
}

object ScalaRadarSchemas {
    val builds = TableQuery[BuildSchema]
}