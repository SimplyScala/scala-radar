package service.build

import akka.actor.{ActorRef, Props, ActorLogging, Actor}
import model.Project
import org.joda.time.DateTime
import scalax.file.Path

object ProjectBuilder {
    val name = "projectBuildManager"

    def props(metadataSubBuilder: MetadataProjectSubBuilder,
              testCodeCoverageSubBuilder: TestCodeCoverageSubBuilder,
              checkstyleSubBuilder: CheckstyleSubBuilder,
              bashExecutor: BashExecutor = BashExecutor) =
        Props(new ProjectBuilder(metadataSubBuilder, testCodeCoverageSubBuilder, checkstyleSubBuilder, bashExecutor))
}

// TODO typer cet acteur
class ProjectBuilder(metadataSubBuilder: MetadataProjectSubBuilder,
                     testCodeCoverageSubBuilder: TestCodeCoverageSubBuilder,
                     checkstyleSubBuilder: CheckstyleSubBuilder,
                     bashExecutor: BashExecutor) extends Actor with ActorLogging {
    def receive = {
        case LaunchProjectBuild(project) =>
            // TODO log the result of clone cmd
            val projectBuildDirectoryPath = bashExecutor.gitCloneProject(project.url)

            val project_withPath = project.copy(path = projectBuildDirectoryPath)

            metadataSubBuilder.ref ! LaunchSubBuild(project_withPath)

            testCodeCoverageSubBuilder.ref ! LaunchSubBuild(project_withPath)

            checkstyleSubBuilder.ref ! LaunchSubBuild(project_withPath)
    }
}

sealed case class LaunchProjectBuild(project: Project)

sealed case class LaunchSubBuild(project: Project)

/*sealed trait SubBuild
case object MetadataBuild extends SubBuild
case object TestCodeCoverageBuild extends SubBuild
case object CheckstyleBuild extends SubBuild

sealed case class SubBuildDone(fromSubBuild: SubBuild)*/

