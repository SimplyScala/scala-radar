package service.build

import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FunSuiteLike, Matchers}
import akka.testkit.{TestProbe, TestActorRef, TestKit}
import akka.actor.ActorSystem
import model.Project
import scalax.file.Path
import testing.tools.ActorTestingTools

class ProjectBuilderTest extends TestKit(ActorSystem("ProjectBuilderTest"))
                              with FunSuiteLike with Matchers with BeforeAndAfterAll
                              with BeforeAndAfter with ActorTestingTools {

    override def afterAll() { system.shutdown() }

    after { closeDummyActors(ProjectBuilder.name) }

    test("when receive LaunchProjectBuild should launch LaunchSubBuild(project) to all SubBuilder (3)") {
        val metadataSubBuilderProbe = TestProbe()
        val metadataSubBuilder = new MetadataProjectSubBuilder(metadataSubBuilderProbe.ref)

        val testCodeCoverageSubBuilderProbe = TestProbe()
        val testCodeCoverageSubBuilder = new TestCodeCoverageSubBuilder(testCodeCoverageSubBuilderProbe.ref)

        val checkstyleSubBuilderProbe = TestProbe()
        val checkstyleSubBuilder = new CheckstyleSubBuilder(checkstyleSubBuilderProbe.ref)


        val underTest = TestActorRef(ProjectBuilder.props(metadataSubBuilder, 
                                                          testCodeCoverageSubBuilder,
                                                          checkstyleSubBuilder,
                                                          StubBashExecutor), 
                                     ProjectBuilder.name)

        underTest ! LaunchProjectBuild(Project("project", "id", "gitUrl", Path("")))

        val msg = LaunchSubBuild(Project("project", "id", "gitUrl", Path("thePath")))
        
        metadataSubBuilderProbe           expectMsg  msg
        testCodeCoverageSubBuilderProbe   expectMsg  msg
        checkstyleSubBuilderProbe         expectMsg  msg
    }
}

object StubBashExecutor extends BashExecutor {
    def gitCloneProject(gitUrl: String): Path = Path("thePath")
}