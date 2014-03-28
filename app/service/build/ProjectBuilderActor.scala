package service.build

import akka.actor._
import model.{Build, Project}
import model.reactive.event.EventProducer
import service.build.ProjectBuilder.SubBuilderFactory
import dao.{ProdDatabase, Dao}
import org.joda.time.DateTime

class ProjectBuilder(val ref: ActorRef) extends SubBuilder {
    def this(context: ActorRefFactory) = this(context.actorOf(ProjectBuilder.props(), ProjectBuilder.name))
}

object ProjectBuilder {
    val name = "projectBuildManager"
    def props(dao: Dao = Dao, subBuilderFactory: SubBuilderFactory = ProjectBuilder.factory, bashExecutor: BashExecutor = BashExecutor) =
        Props(new ProjectBuilderActor(subBuilderFactory, bashExecutor, dao))

    type SubBuilderFactory = (SubBuilderName) => (ActorRefFactory) => SubBuilder

    val factory: SubBuilderFactory = (subBuilder: SubBuilderName) =>
        subBuilder match {
            case TestCodeCoverageSubBuilderName => (context: ActorRefFactory) => new TestCodeCoverageSubBuilder(context)
            case CheckstyleSubBuilderName => (context: ActorRefFactory) => new CheckstyleSubBuilder(context)
        }
}

class ProjectBuilderActor(subBuilderFactory: SubBuilderFactory, bashExecutor: BashExecutor, dao: Dao) extends Actor
    with ActorLogging with ProdDatabase {

    // TODO context -> implicit ???
    val testCodeCoverageSubBuilder = subBuilderFactory(TestCodeCoverageSubBuilderName)(context)
    val checkstyleSubBuilder = subBuilderFactory(CheckstyleSubBuilderName)(context)

    var eventProducers = Map[String, EventProducer[ProjectBuildEvent]]()
    var buildDone: List[Boolean] = Nil

    def receive = {
        case LaunchProjectBuild(project, eventProducer) =>    // TODO a mettre dans le constructeur ?
            launchSubBuilds( cloneProjectFromDistantRepo(project, eventProducer) )

        case SubBuildDone(fromSubBuild) =>
            buildDone :+= true
            pushEventToWebClient(fromSubBuild.build.project, fromSubBuild.toBuildEvent)
            doneBuild(fromSubBuild)

        // TODO implements this case
        case SubBuildFailed(fromSubBuild) =>
            log.error(s"$fromSubBuild failed")
            eventProducers.get(fromSubBuild.build.project.name).map { _.channel.push(ScctFailed(fromSubBuild.build.project)) }
    }

    private def cloneProjectFromDistantRepo(project: Project, eventProducer: EventProducer[ProjectBuildEvent]): Build = {
        val startDate = DateTime.now()

        eventProducers += ( project.name -> eventProducer )
        // TODO log the result of clone cmd
        val projectBuildDirectoryPath = bashExecutor.gitCloneProject(project, startDate)

        eventProducers.get(project.name).map { producer => producer.channel.push(ProjectCloned(project)) }

        project.copy(path = projectBuildDirectoryPath).toBuild(startDate)
    }

    private def launchSubBuilds(build: Build) {
        testCodeCoverageSubBuilder.ref ! LaunchSubBuild(build)
        checkstyleSubBuilder.ref ! LaunchSubBuild(build)
    }

    private def doneBuild(fromSubBuild: SubBuild) {
        if(allSubBuildAreDone) {
            pushEventToWebClient(fromSubBuild.build.project, BuildDone(fromSubBuild.build.project))
            dao.save(fromSubBuild.build.toSuccessfulBuild())
            context.stop(self)
        }
    }

    private def pushEventToWebClient(channelId: Project, event: ProjectBuildEvent) =
        eventProducers.get(channelId.name).map { _.channel.push(event) }

    private def allSubBuildAreDone: Boolean = buildDone.size == 2
}

// TODO faire le ménage là dedans

sealed case class LaunchProjectBuild(project: Project, eventProducer: EventProducer[ProjectBuildEvent])
sealed case class LaunchSubBuild(build: Build)

sealed trait SubBuildResult { def fromSubBuild: SubBuild }
sealed case class SubBuildDone(fromSubBuild: SubBuild) extends SubBuildResult
sealed case class SubBuildFailed(fromSubBuild: SubBuild) extends SubBuildResult

trait SubBuilder { def ref: ActorRef }
trait SubBuilderName

sealed trait SubBuild {
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
sealed case class ScctFailed(project: Project) extends ProjectBuildEvent {
    def eventName: String = "scctFailed"
}
sealed case class CheckstyleDone(project: Project) extends ProjectBuildEvent {
    def eventName: String = "checkstyleDone"
}
case class BuildDone(project: Project) extends ProjectBuildEvent { def eventName: String = "buildDone" }