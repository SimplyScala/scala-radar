package service.build

import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FunSuiteLike, Matchers}
import akka.testkit.{TestProbe, TestActorRef, TestKit}
import akka.actor.{ActorRefFactory, ActorSystem}
import model.Project
import scalax.file.Path
import testing.tools.ActorTestingTools

class ProjectBuilderTest extends TestKit(ActorSystem("ProjectBuilderTest"))
                              with FunSuiteLike with Matchers with BeforeAndAfterAll
                              with BeforeAndAfter with ActorTestingTools {

    override def afterAll() { system.shutdown() }

    after { closeDummyActors(ProjectBuilder.name) }

    test("when receive LaunchProjectBuild should launch LaunchSubBuild(project) to all SubBuilder (3)") {
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
                    
        val underTest = TestActorRef(ProjectBuilder.props(subBuilderFactoryStub, StubBashExecutor), ProjectBuilder.name)

        underTest ! LaunchProjectBuild(Project("project", "gitUrl", Path("")))

        val msg = LaunchSubBuild(Project("project", "gitUrl", Path("thePath")))
        
        metadataProbe           expectMsg  msg
        testCodeCoverageProbe   expectMsg  msg
        checkstyleProbe         expectMsg  msg
    }
}

object StubBashExecutor extends BashExecutor {
    def gitCloneProject(project: Project): Path = Path("thePath")
}