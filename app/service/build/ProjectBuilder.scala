package service.build

import akka.actor._
import model.Project
import service.build.SubBuilderFactory.SubBuilderFactory
import model.reactive.event.EventProducer

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
class ProjectBuilder(subBuilderFactory: SubBuilderFactory, bashExecutor: BashExecutor) extends Actor with ActorLogging {

    //val metadataSubBuilder = subBuilderFactory(MetadataProjectSubBuilderName)(context)
    val testCodeCoverageSubBuilder = subBuilderFactory(TestCodeCoverageSubBuilderName)(context)
    val checkstyleSubBuilder = subBuilderFactory(CheckstyleSubBuilderName)(context)

    var eventProducers = Map[String, EventProducer[ProjectBuildEvent]]()
    
    def receive = {
        case LaunchProjectBuild(project, eventProducer) =>
            eventProducers += (project.name -> eventProducer)    // TODO attention ne pas en faire une ressource multi partagée au sein de l'acteur
            // TODO log the result of clone cmd
            val projectBuildDirectoryPath = bashExecutor.gitCloneProject(project)

            eventProducers.get(project.name).map { producer => producer.channel.push(ProjectCloned(project)) }

            val project_withPath = project.copy(path = projectBuildDirectoryPath)

            //metadataSubBuilder.ref ! LaunchSubBuild(project_withPath)

            testCodeCoverageSubBuilder.ref ! LaunchSubBuild(project_withPath)

            checkstyleSubBuilder.ref ! LaunchSubBuild(project_withPath)

        case SubBuildDone(fromSubBuild) => 
            eventProducers.get(fromSubBuild.project.name).map { producer =>
                producer.channel.push(fromSubBuild.toBuildEvent)
            }
    }
}

sealed trait ProjectBuildEvent { def eventName: String }
sealed case class ProjectCloned(project: Project) extends ProjectBuildEvent {
    def eventName: String = "projectCloned"
}
sealed case class ScctDone(project: Project) extends ProjectBuildEvent {
    def eventName: String = "scctDone"
}
sealed case class CheckstyleDone(project: Project) extends ProjectBuildEvent {
    def eventName: String = "checkstyleDone"
}

sealed case class LaunchProjectBuild(project: Project, eventProducer: EventProducer[ProjectBuildEvent])
sealed case class LaunchSubBuild(project: Project)

trait SubBuilder { def ref: ActorRef }
trait SubBuilderName

sealed trait SubBuild {
    def project: Project
    def toBuildEvent: ProjectBuildEvent
}
/*case class MetadataSubBuild(project: Project) extends SubBuild {
    def toBuildEvent: ProjectBuildEvent =
}*/
sealed case class TestCodeCoverageBuild(project: Project) extends SubBuild {
    def toBuildEvent: ProjectBuildEvent = ScctDone(project)
}

sealed case class CheckstyleBuild(project: Project) extends SubBuild {
    def toBuildEvent: ProjectBuildEvent = CheckstyleDone(project)
}

sealed case class SubBuildDone(fromSubBuild: SubBuild)