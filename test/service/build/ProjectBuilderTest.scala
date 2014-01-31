package service.build

import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FunSuiteLike, Matchers}
import akka.testkit.{TestProbe, TestActorRef, TestKit}
import akka.actor.{ActorRefFactory, ActorSystem}
import model.Project
import scalax.file.Path
import testing.tools.ActorTestingTools
import model.reactive.event.EventProducer
import org.scalatest.mock.MockitoSugar
import play.api.libs.iteratee.Concurrent.Channel
import org.mockito.Mockito._

class ProjectBuilderTest extends TestKit(ActorSystem("ProjectBuilderTest"))
                              with FunSuiteLike with Matchers with BeforeAndAfterAll
                              with BeforeAndAfter with ActorTestingTools with MockitoSugar {

    override def afterAll() { system.shutdown() }

    after { closeDummyActors(ProjectBuilder.name) }

    test("when receive LaunchProjectBuild should launch LaunchSubBuild(project) to all SubBuilder (2)") {
        val metadataProbe = TestProbe()
        val testCodeCoverageProbe = TestProbe()
        val checkstyleProbe = TestProbe()

        val subBuilderFactoryStub = { (subBuilder: SubBuilderName) =>
            val subBuilderInstance = subBuilder match {
                case MetadataProjectSubBuilderName => new MetadataProjectSubBuilder(metadataProbe.ref) 
                case TestCodeCoverageSubBuilderName => new TestCodeCoverageSubBuilder(testCodeCoverageProbe.ref)
                case CheckstyleSubBuilderName => new CheckstyleSubBuilder(checkstyleProbe.ref)
            }

            (_: ActorRefFactory) => subBuilderInstance
        }

        val stubEventProvider = mock[EventProducer[ProjectBuildEvent]]
        val stubChannel = mock[Channel[ProjectBuildEvent]]
        when(stubEventProvider.channel).thenReturn(stubChannel)

        val underTest = TestActorRef(ProjectBuilder.props(subBuilderFactoryStub, StubBashExecutor), ProjectBuilder.name)

        underTest ! LaunchProjectBuild(Project("project", "gitUrl", Path("")), stubEventProvider)

        val msg = LaunchSubBuild(Project("project", "gitUrl", Path("thePath")))
        
//        metadataProbe           expectMsg  msg
        testCodeCoverageProbe   expectMsg  msg
        checkstyleProbe         expectMsg  msg
    }
}

object StubBashExecutor extends BashExecutor {
    def gitCloneProject(project: Project): Path = Path("thePath")
}