package service.build

import akka.actor._
import model.Project
import service.build.SubBuilderFactory.SubBuilderFactory

object SubBuilderFactory {
    type SubBuilderFactory = (SubBuilderName) => (ActorRefFactory) => SubBuilder

    val factory: SubBuilderFactory = (subBuilder: SubBuilderName) =>
        subBuilder match {
            case MetadataProjectSubBuilderName => (context: ActorRefFactory) => new MetadataProjectSubBuilder(context)
            case TestCodeCoverageSubBuilderName => (context: ActorRefFactory) => new TestCodeCoverageSubBuilder(context)
            case CheckstyleSubBuilderName => (context: ActorRefFactory) => new CheckstyleSubBuilder(context)
        }
}

object ProjectBuilder {
    val name = "projectBuildManager"

    def props(subBuilderFactory: SubBuilderFactory = SubBuilderFactory.factory, bashExecutor: BashExecutor = BashExecutor) =
        Props(new ProjectBuilder(subBuilderFactory, bashExecutor))
}

// TODO typer cet acteur
class ProjectBuilder(subBuilderFactory: SubBuilderFactory,
                     bashExecutor: BashExecutor)
    extends Actor with ActorLogging {

    val metadataSubBuilder = subBuilderFactory(MetadataProjectSubBuilderName)(context)
    val testCodeCoverageSubBuilder = subBuilderFactory(TestCodeCoverageSubBuilderName)(context)
    val checkstyleSubBuilder = subBuilderFactory(CheckstyleSubBuilderName)(context)

    def receive = {
        case LaunchProjectBuild(project) =>
            // TODO log the result of clone cmd
            val projectBuildDirectoryPath = bashExecutor.gitCloneProject(project)

            val project_withPath = project.copy(path = projectBuildDirectoryPath)

            metadataSubBuilder.ref ! LaunchSubBuild(project_withPath)

            testCodeCoverageSubBuilder.ref ! LaunchSubBuild(project_withPath)

            checkstyleSubBuilder.ref ! LaunchSubBuild(project_withPath)
    }
}

sealed case class LaunchProjectBuild(project: Project)
sealed case class LaunchSubBuild(project: Project)

trait SubBuilder { def ref: ActorRef }
trait SubBuilderName

/*sealed trait SubBuild
case object MetadataBuild extends SubBuild
case object TestCodeCoverageBuild extends SubBuild
case object CheckstyleBuild extends SubBuild

sealed case class SubBuildDone(fromSubBuild: SubBuild)*/

