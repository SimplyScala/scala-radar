package service.build

import akka.testkit.{TestProbe, TestActorRef, TestKit}
import akka.actor.ActorSystem
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FunSuiteLike}
import testing.tools.ActorTestingTools
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import model.{Project, Build}
import org.joda.time.DateTime
import scalax.file.Path
import scala.util.Try
import service.build.BashExecutor.Logs

class TestCodeCoverageSubBuilderActorTest extends TestKit(ActorSystem("TestCodeCoverageSubBuilderActorTest"))
        with FunSuiteLike with org.scalatest.Matchers with BeforeAndAfterAll
        with BeforeAndAfter with ActorTestingTools with MockitoSugar {

    override def afterAll() { system.shutdown() }

    after { closeDummyActors(TestCodeCoverageSubBuilder.name) }

    val build = Build("buildId", DateTime.now(), Project("project", "url", Path("")))

    test("when receive LaunchSubBuild(build) message & test code coverage action is Succeed, " +
         "should send ProjectBuilderActor ! SubBuildDone(TestCodeCoverageBuild(build))") {
        val parent = TestProbe()
        val underTest = TestActorRef(TestCodeCoverageSubBuilder.props(stubBashExecutor(true)),parent.ref, TestCodeCoverageSubBuilder.name)

        // When
        underTest ! LaunchSubBuild(build)

        // Then
        parent.expectMsg(SubBuildDone(TestCodeCoverageBuild(build)))
    }

    test("when receive LaunchSubBuild(build) message & test code coverage action is Failed, " +
         "should send ProjectBuilderActor ! SubBuildFailed(TestCodeCoverageBuild(build))") {
        val parent = TestProbe()
        val underTest = TestActorRef(TestCodeCoverageSubBuilder.props(stubBashExecutor(false)),parent.ref, TestCodeCoverageSubBuilder.name)

        // When
        underTest ! LaunchSubBuild(build)

        // Then
        parent.expectMsg(SubBuildFailed(TestCodeCoverageBuild(build)))
    }

    def stubBashExecutor(returnSuccess: Boolean): BashExecutor = {
        val stubBashExecutor = mock[BashExecutor]

        val response: Try[Logs] = Try {
            if(returnSuccess) "OK" else throw new Exception("erreur lors de l'éxécution de la commande scct:test")
        }

        when(stubBashExecutor.executeScctTestCmd(build)).thenReturn(response)

        stubBashExecutor
    }
}