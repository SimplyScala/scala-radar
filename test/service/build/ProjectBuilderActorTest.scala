package service.build

import org.scalatest._
import akka.testkit.{TestProbe, TestActorRef, TestKit}
import akka.actor.{ActorRefFactory, ActorSystem}
import scalax.file.Path
import testing.tools.ActorTestingTools
import model.reactive.event.EventProducer
import org.scalatest.mock.MockitoSugar
import play.api.libs.iteratee.Concurrent.Channel
import org.mockito.Mockito._
import model.Project
import service.build.SubBuilderFactory.SubBuilderFactory

class ProjectBuilderActorTest extends TestKit(ActorSystem("ProjectBuilderTest"))
                              with FunSuiteLike with Matchers with BeforeAndAfterAll
                              with BeforeAndAfter with ActorTestingTools with MockitoSugar {

    override def afterAll() { system.shutdown() }

    after { closeDummyActors(ProjectBuilderActor.name) }

    test("when receive LaunchProjectBuild should launch LaunchSubBuild(project) to all SubBuilder (2)") {
        val testCodeCoverageProbe = TestProbe()
        val checkstyleProbe = TestProbe()

        val (stubEventProvider, _) = eventProducer()

        val underTest = TestActorRef(ProjectBuilderActor.props(subBuilderFactoryStub(testCodeCoverageProbe, checkstyleProbe), stubBashExecutor), ProjectBuilderActor.name)

        underTest ! LaunchProjectBuild(Project("project", "gitUrl", Path("")), stubEventProvider)

        val msg = LaunchSubBuild(Project("project", "gitUrl", Path("thePath")))

        testCodeCoverageProbe   expectMsg  msg
        checkstyleProbe         expectMsg  msg
    }

    test("when receive LaunchProjectBuild should push ProjectCloned(project) into corresponding event channel") {
        val testCodeCoverageProbe = TestProbe()
        val checkstyleProbe = TestProbe()

        val (stubEventProvider, stubChannel) = eventProducer()

        val underTest = TestActorRef(ProjectBuilderActor.props(subBuilderFactoryStub(testCodeCoverageProbe, checkstyleProbe), stubBashExecutor), ProjectBuilderActor.name)

        underTest ! LaunchProjectBuild(Project("project", "gitUrl", Path("")), stubEventProvider)

        verify(stubChannel).push(ProjectCloned(Project("project", "gitUrl", Path(""))))
    }

    test("when receive SubBuildDone(TestCodeCoverageBuild) should push ScctDone event into corresponding event channel") {
        val testCodeCoverageProbe = TestProbe()
        val checkstyleProbe = TestProbe()

        val (stubEventProvider, stubChannel) = eventProducer()

        val underTest = TestActorRef(ProjectBuilderActor.props(subBuilderFactoryStub(testCodeCoverageProbe, checkstyleProbe), stubBashExecutor), ProjectBuilderActor.name)

        underTest ! LaunchProjectBuild(Project("project", "gitUrl", Path("")), stubEventProvider)
        underTest ! SubBuildDone(TestCodeCoverageBuild(Project("project", "gitUrl", Path(""))))

        verify(stubChannel).push(ScctDone(Project("project", "gitUrl", Path(""))))
    }

    test("when receive SubBuildDone(CheckstyleBuild) should push CheckstyleDone event into corresponding event channel") {
        val testCodeCoverageProbe = TestProbe()
        val checkstyleProbe = TestProbe()

        val (stubEventProvider, stubChannel) = eventProducer()

        val underTest = TestActorRef(ProjectBuilderActor.props(subBuilderFactoryStub(testCodeCoverageProbe, checkstyleProbe), stubBashExecutor), ProjectBuilderActor.name)

        underTest ! LaunchProjectBuild(Project("project", "gitUrl", Path("")), stubEventProvider)
        underTest ! SubBuildDone(CheckstyleBuild(Project("project", "gitUrl", Path(""))))

        verify(stubChannel).push(CheckstyleDone(Project("project", "gitUrl", Path(""))))
    }

    test("if all subBuilds is done when receive SubBuildDone(any[SubBuild]) should push BuildDone event in to corresponding event channel") {
        val testCodeCoverageProbe = TestProbe()
        val checkstyleProbe = TestProbe()

        val (stubEventProvider, stubChannel) = eventProducer()

        val underTest = TestActorRef(ProjectBuilderActor.props(subBuilderFactoryStub(testCodeCoverageProbe, checkstyleProbe), stubBashExecutor), ProjectBuilderActor.name)

        underTest ! LaunchProjectBuild(Project("project", "gitUrl", Path("")), stubEventProvider)
        underTest ! SubBuildDone(CheckstyleBuild(Project("project", "gitUrl", Path(""))))
        underTest ! SubBuildDone(TestCodeCoverageBuild(Project("project", "gitUrl", Path(""))))

        verify(stubChannel).push(BuildDone(Project("project", "gitUrl", Path(""))))
    }

    private def eventProducer(): (EventProducer[ProjectBuildEvent], Channel[ProjectBuildEvent]) = {
        val stubChannel = mock[Channel[ProjectBuildEvent]]
        val stubEventProvider = mock[EventProducer[ProjectBuildEvent]]
        when(stubEventProvider.channel).thenReturn(stubChannel)

        (stubEventProvider, stubChannel)
    }

    private def subBuilderFactoryStub(testCodeCoverageProbe: TestProbe, checkstyleProbe: TestProbe): SubBuilderFactory = {
        (subBuilder: SubBuilderName) => {
            val subBuilderInstance = subBuilder match {
                case TestCodeCoverageSubBuilderName => new TestCodeCoverageSubBuilder(testCodeCoverageProbe.ref)
                case CheckstyleSubBuilderName => new CheckstyleSubBuilder(checkstyleProbe.ref)
            }

            (_: ActorRefFactory) => subBuilderInstance
        }
    }

    private val stubBashExecutor: BashExecutor = {
        val stubBashExecutor = mock[BashExecutor]
        when(stubBashExecutor.gitCloneProject(Project("project", "gitUrl", Path("")))).thenReturn(Path("thePath"))

        stubBashExecutor
    }
}