package service.build

import akka.actor._
import model.Project
import model.reactive.event.EventProducer
import service.build.ProjectBuilder.SubBuilderFactory

class ProjectBuilder(val ref: ActorRef) extends SubBuilder {
    def this(context: ActorRefFactory) = this(context.actorOf(ProjectBuilder.props(), ProjectBuilder.name))
}

object ProjectBuilder {
    val name = "projectBuildManager"
    def props(subBuilderFactory: SubBuilderFactory = ProjectBuilder.factory, bashExecutor: BashExecutor = BashExecutor) =
        Props(new ProjectBuilderActor(subBuilderFactory, bashExecutor))

    type SubBuilderFactory = (SubBuilderName) => (ActorRefFactory) => SubBuilder
    val factory: SubBuilderFactory = (subBuilder: SubBuilderName) =>
        subBuilder match {
            case TestCodeCoverageSubBuilderName => (context: ActorRefFactory) => new TestCodeCoverageSubBuilder(context)
            case CheckstyleSubBuilderName => (context: ActorRefFactory) => new CheckstyleSubBuilder(context)
        }
}

class ProjectBuilderActor(subBuilderFactory: SubBuilderFactory, bashExecutor: BashExecutor) extends Actor with ActorLogging {

    // TODO context -> implicit ???
    val testCodeCoverageSubBuilder = subBuilderFactory(TestCodeCoverageSubBuilderName)(context)
    val checkstyleSubBuilder = subBuilderFactory(CheckstyleSubBuilderName)(context)

    var eventProducers = Map[String, EventProducer[ProjectBuildEvent]]()
    var buildDone: List[Boolean] = Nil

    def receive = {
        case LaunchProjectBuild(project, eventProducer) =>
            eventProducers += (project.name -> eventProducer)    // TODO attention ne pas en faire une ressource multi partagée au sein de l'acteur
            // TODO log the result of clone cmd
            val projectBuildDirectoryPath = bashExecutor.gitCloneProject(project)

            eventProducers.get(project.name).map { producer => producer.channel.push(ProjectCloned(project)) }

            val project_withPath = project.copy(path = projectBuildDirectoryPath)

            testCodeCoverageSubBuilder.ref ! LaunchSubBuild(project_withPath)

            checkstyleSubBuilder.ref ! LaunchSubBuild(project_withPath)

        case SubBuildDone(fromSubBuild) => 
            eventProducers.get(fromSubBuild.project.name).map { producer =>
                buildDone :+= true
                producer.channel.push(fromSubBuild.toBuildEvent)

                if(allSubBuildAreDone)
                    producer.channel.push(BuildDone(fromSubBuild.project))
                    // TODO save Build
                    // TODO tuer l'acteur
            }
    }

    private def allSubBuildAreDone: Boolean = buildDone.size == 2
}

sealed case class LaunchProjectBuild(project: Project, eventProducer: EventProducer[ProjectBuildEvent])
sealed case class SubBuildDone(fromSubBuild: SubBuild)
sealed case class LaunchSubBuild(project: Project)

trait SubBuilder { def ref: ActorRef }
trait SubBuilderName

sealed trait SubBuild {
    def project: Project
    def toBuildEvent: ProjectBuildEvent
}
sealed case class TestCodeCoverageBuild(project: Project) extends SubBuild {
    def toBuildEvent: ProjectBuildEvent = ScctDone(project)
}
sealed case class CheckstyleBuild(project: Project) extends SubBuild {
    def toBuildEvent: ProjectBuildEvent = CheckstyleDone(project)
}

sealed trait ProjectBuildEvent { def eventName: String; def project: Project }
sealed case class ProjectCloned(project: Project) extends ProjectBuildEvent {
    def eventName: String = "projectCloned"
}
sealed case class ScctDone(project: Project) extends ProjectBuildEvent {
    def eventName: String = "scctDone"
}
sealed case class CheckstyleDone(project: Project) extends ProjectBuildEvent {
    def eventName: String = "checkstyleDone"
}
case class BuildDone(project: Project) extends ProjectBuildEvent { def eventName: String = "buildDone" }