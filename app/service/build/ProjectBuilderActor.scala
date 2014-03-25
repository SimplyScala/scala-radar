package service.build

import akka.actor._
import model.{Build, SuccessfulBuild, Project}
import model.reactive.event.EventProducer
import service.build.ProjectBuilder.SubBuilderFactory

class ProjectBuilder(val ref: ActorRef) extends SubBuilder {
    def this(context: ActorRefFactory, db: sorm.Instance) = this(context.actorOf(ProjectBuilder.props(db), ProjectBuilder.name))
}

object ProjectBuilder {
    val name = "projectBuildManager"
    def props(db: sorm.Instance, subBuilderFactory: SubBuilderFactory = ProjectBuilder.factory, bashExecutor: BashExecutor = BashExecutor) =
        Props(new ProjectBuilderActor(subBuilderFactory, bashExecutor, db))

    type SubBuilderFactory = (SubBuilderName) => (ActorRefFactory) => SubBuilder
    val factory: SubBuilderFactory = (subBuilder: SubBuilderName) =>
        subBuilder match {
            case TestCodeCoverageSubBuilderName => (context: ActorRefFactory) => new TestCodeCoverageSubBuilder(context)
            case CheckstyleSubBuilderName => (context: ActorRefFactory) => new CheckstyleSubBuilder(context)
        }
}

// TODO use Dao instead of sorm.Instance
class ProjectBuilderActor(subBuilderFactory: SubBuilderFactory, bashExecutor: BashExecutor, db: sorm.Instance) extends Actor with ActorLogging {

    // TODO context -> implicit ???
    val testCodeCoverageSubBuilder = subBuilderFactory(TestCodeCoverageSubBuilderName)(context)
    val checkstyleSubBuilder = subBuilderFactory(CheckstyleSubBuilderName)(context)

    var eventProducers = Map[String, EventProducer[ProjectBuildEvent]]()
    var buildDone: List[Boolean] = Nil

    def receive = {
        case LaunchProjectBuild(project, eventProducer) =>    // TODO a mettre dans le constructeur ?
            eventProducers += (project.name -> eventProducer)
            // TODO log the result of clone cmd
            val projectBuildDirectoryPath = bashExecutor.gitCloneProject(project)

            eventProducers.get(project.name).map { producer => producer.channel.push(ProjectCloned(project)) }

            val project_withPath = project.copy(path = projectBuildDirectoryPath)

            val build = project_withPath.toBuild()

            testCodeCoverageSubBuilder.ref ! LaunchSubBuild(build)
            checkstyleSubBuilder.ref ! LaunchSubBuild(build)

        case SubBuildDone(fromSubBuild) =>
            buildDone :+= true
            pushEventToWebClient(fromSubBuild.build.project, fromSubBuild.toBuildEvent)
            doneBuild(fromSubBuild)
    }

    private def doneBuild(fromSubBuild: SubBuild) {
        if(allSubBuildAreDone) {
            pushEventToWebClient(fromSubBuild.build.project, BuildDone(fromSubBuild.build.project))
            db.save(fromSubBuild.build.toSuccessfulBuild())
            context.stop(self)
        }
    }

    private def pushEventToWebClient(channelId: Project, event: ProjectBuildEvent) =
        eventProducers.get(channelId.name).map { _.channel.push(event) }

    private def allSubBuildAreDone: Boolean = buildDone.size == 2
}

sealed case class LaunchProjectBuild(project: Project, eventProducer: EventProducer[ProjectBuildEvent])
sealed case class SubBuildDone(fromSubBuild: SubBuild)
sealed case class LaunchSubBuild(build: Build)

trait SubBuilder { def ref: ActorRef }
trait SubBuilderName

sealed trait SubBuild {
    //def project: Project
    def build: Build
    def toBuildEvent: ProjectBuildEvent
}
sealed case class TestCodeCoverageBuild(build: Build) extends SubBuild {
    def toBuildEvent: ProjectBuildEvent = ScctDone(build.project)
}
sealed case class CheckstyleBuild(build: Build) extends SubBuild {
    def toBuildEvent: ProjectBuildEvent = CheckstyleDone(build.project)
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