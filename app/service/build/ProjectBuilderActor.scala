package service.build

import akka.actor._
import model.reactive.event._
import service.build.ProjectBuilder.SubBuilderFactory
import dao.{ProdDatabase, Dao}
import org.joda.time.DateTime
import model.Build
import model.Project
import model.reactive.event.ScctDone
import model.reactive.event.CheckstyleDone
import model.build.{SubBuild, CheckstyleSubBuilderName, TestCodeCoverageSubBuilderName, SubBuilderName}

class ProjectBuilder(val ref: ActorRef) {
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

        case SubBuildSucceed(fromSubBuild) =>
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

sealed trait SubBuildDone { def fromSubBuild: SubBuild }
sealed case class SubBuildSucceed(fromSubBuild: SubBuild) extends SubBuildDone
sealed case class SubBuildFailed(fromSubBuild: SubBuild) extends SubBuildDone

trait SubBuilder { def ref: ActorRef }
