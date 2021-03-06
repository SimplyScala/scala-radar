package service.build

import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FunSuiteLike}
import akka.testkit.{TestProbe, TestActorRef, TestKit}
import akka.actor.{ActorRefFactory, ActorSystem}
import scalax.file.Path
import testing.tools.{StubDatabase, ActorTestingTools}
import model.reactive.event._
import org.scalatest.mock.MockitoSugar
import play.api.libs.iteratee.Concurrent.Channel
import service.build.ProjectBuilder.SubBuilderFactory
import org.mockito.Mockito._
import org.joda.time.DateTime
import org.mockito.Matchers._
import dao.Dao
import org.mockito.Matchers
import model.build._
import model.reactive.event.ProjectCloned
import model.build.CheckstyleBuild
import model.Build
import model.Project
import model.build.TestCodeCoverageBuild
import model.SuccessfulBuild

class ProjectBuilderActorTest extends TestKit(ActorSystem("ProjectBuilderActorTest"))
                              with FunSuiteLike with org.scalatest.Matchers with BeforeAndAfterAll
                              with BeforeAndAfter with ActorTestingTools with MockitoSugar with StubDatabase {

    override def afterAll() { system.shutdown() }

    after { closeDummyActors(ProjectBuilder.name) }

    test("when receive LaunchProjectBuild should launch LaunchSubBuild(project) to all SubBuilder (2)") {
        val testCodeCoverageProbe = TestProbe()
        val checkstyleProbe = TestProbe()

        val expectedProject = Project("project", "gitUrl")

        val (stubEventProvider, _) = stubEventProducer()

        val underTest = TestActorRef(ProjectBuilder.props(null, subBuilderFactoryStub(testCodeCoverageProbe, checkstyleProbe), stubBashExecutor), ProjectBuilder.name)

        // When
        underTest ! LaunchProjectBuild(expectedProject, stubEventProvider)

        // Then
        val result1 = testCodeCoverageProbe   expectMsgClass classOf[LaunchSubBuild]
        val result2 = checkstyleProbe         expectMsgClass classOf[LaunchSubBuild]

        result1.build.project shouldBe expectedProject
        result2.build.project shouldBe expectedProject
    }

    test("when receive LaunchProjectBuild should push ProjectCloned(project) into corresponding event channel") {
        val testCodeCoverageProbe = TestProbe()
        val checkstyleProbe = TestProbe()

        val (stubEventProvider, stubChannel) = stubEventProducer()

        val underTest = TestActorRef(ProjectBuilder.props(null, subBuilderFactoryStub(testCodeCoverageProbe, checkstyleProbe), stubBashExecutor), ProjectBuilder.name)

        // When
        underTest ! LaunchProjectBuild(Project("project", "gitUrl"), stubEventProvider)

        // Then
        verify(stubChannel).push(ProjectCloned(Project("project", "gitUrl")))
    }

    test("when receive SubBuildDone(TestCodeCoverageBuild) should push ScctDone event into corresponding event channel") {
        val testCodeCoverageProbe = TestProbe()
        val checkstyleProbe = TestProbe()

        val (stubEventProvider, stubChannel) = stubEventProducer()

        val underTest = TestActorRef(ProjectBuilder.props(null, subBuilderFactoryStub(testCodeCoverageProbe, checkstyleProbe), stubBashExecutor), ProjectBuilder.name)

        // When
        underTest ! LaunchProjectBuild(Project("project", "gitUrl"), stubEventProvider)
        underTest ! SubBuildSucceed(TestCodeCoverageBuild(Build("id", DateTime.now(), Path(""), Project("project", "gitUrl"))))

        // Then
        verify(stubChannel).push(ScctDone(Project("project", "gitUrl")))
    }

    test("when receive SubBuildFailed(TestCodeCoverageBuild) should push ScctFailed event into corresponding event channel") {
        val testCodeCoverageProbe = TestProbe()
        val checkstyleProbe = TestProbe()

        val (stubEventProvider, stubChannel) = stubEventProducer()

        val underTest = TestActorRef(ProjectBuilder.props(null, subBuilderFactoryStub(testCodeCoverageProbe, checkstyleProbe), stubBashExecutor), ProjectBuilder.name)

        // When
        underTest ! LaunchProjectBuild(Project("project", "gitUrl"), stubEventProvider)
        underTest ! SubBuildFailed(TestCodeCoverageBuild(Build("id", DateTime.now(), Path(""), Project("project", "gitUrl"))))

        // Then
        verify(stubChannel).push(ScctFailed(Project("project", "gitUrl")))
    }

    test("when receive SubBuildDone(CheckstyleBuild) should push CheckstyleDone event into corresponding event channel") {
        val testCodeCoverageProbe = TestProbe()
        val checkstyleProbe = TestProbe()

        val (stubEventProvider, stubChannel) = stubEventProducer()

        val underTest = TestActorRef(ProjectBuilder.props(null, subBuilderFactoryStub(testCodeCoverageProbe, checkstyleProbe), stubBashExecutor), ProjectBuilder.name)

        // When
        underTest ! LaunchProjectBuild(Project("project", "gitUrl"), stubEventProvider)
        underTest ! SubBuildSucceed(CheckstyleBuild(Project("project", "gitUrl").toBuild(Path(""))))

        // Then
        verify(stubChannel).push(CheckstyleDone(Project("project", "gitUrl")))
    }

    test("if all subBuilds is done after receive SubBuildDone(any[SubBuild]) should push BuildDone event into corresponding event channel") {
        val testCodeCoverageProbe = TestProbe()
        val checkstyleProbe = TestProbe()

        val (stubEventProvider, stubChannel) = stubEventProducer()

        val underTest = TestActorRef(ProjectBuilder.props(mock[Dao], subBuilderFactoryStub(testCodeCoverageProbe, checkstyleProbe), stubBashExecutor), ProjectBuilder.name)

        val expectedProject = Project("name", "url")

        // When
        underTest ! LaunchProjectBuild(expectedProject, stubEventProvider)
        underTest ! SubBuildSucceed(CheckstyleBuild(Build("id", DateTime.now(), Path(""), expectedProject)))
        underTest ! SubBuildSucceed(TestCodeCoverageBuild(Build("id", DateTime.now(), Path(""), expectedProject)))

        // Then
        verify(stubChannel).push(BuildDone(expectedProject))
    }

    test("if all subBuilds is done after receive SubBuildDone(any[SubBuild]) should save SuccessfulBuild") {
        val testCodeCoverageProbe = TestProbe()
        val checkstyleProbe = TestProbe()

        val dao = mock[Dao]

        val (stubEventProvider, _) = stubEventProducer()
        val expectedProject = Project("name", "url")

        val underTest = TestActorRef(ProjectBuilder.props(dao, subBuilderFactoryStub(testCodeCoverageProbe, checkstyleProbe), stubBashExecutor), ProjectBuilder.name)

        // When
        underTest ! LaunchProjectBuild(expectedProject, stubEventProvider)
        underTest ! SubBuildSucceed(CheckstyleBuild(Build("id", DateTime.now(), Path(""), expectedProject)))
        underTest ! SubBuildSucceed(TestCodeCoverageBuild(Build("id", DateTime.now(), Path(""), expectedProject)))

        // Then
        verify(dao).save(SuccessfulBuild("id", anyLong(), anyLong(), "", expectedProject.name, expectedProject.gitUrl))
    }

    test("if all subBuilds is done after receive SubBuildDone(any[SubBuild]) should kill itself") {
        val testCodeCoverageProbe = TestProbe()
        val checkstyleProbe = TestProbe()

        val dao = mock[Dao]

        val (stubEventProvider, _) = stubEventProducer()
        val expectedProject = Project("name", "url")

        val underTest = TestActorRef(ProjectBuilder.props(dao, subBuilderFactoryStub(testCodeCoverageProbe, checkstyleProbe), stubBashExecutor), ProjectBuilder.name)

        watch(underTest)

        // When
        underTest ! LaunchProjectBuild(expectedProject, stubEventProvider)
        underTest ! SubBuildSucceed(CheckstyleBuild(Build("id", DateTime.now(), Path(""), expectedProject)))
        underTest ! SubBuildSucceed(TestCodeCoverageBuild(Build("id", DateTime.now(), Path(""), expectedProject)))

        // Then
        expectTerminated(underTest)

        unwatch(underTest)
    }

    private def stubEventProducer(): (EventProducer[ProjectBuildEvent], Channel[ProjectBuildEvent]) = {
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
        when(stubBashExecutor.gitCloneProject(Matchers.eq(Project("project", "gitUrl")), any[DateTime])).thenReturn(Path("thePath"))

        stubBashExecutor
    }
}