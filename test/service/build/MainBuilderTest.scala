package service.build

import akka.testkit.{TestProbe, TestActorRef, TestKit}
import akka.actor.{ActorRefFactory, ActorSystem}
import org.scalatest.{BeforeAndAfter, Matchers, BeforeAndAfterAll, FunSuiteLike}
import testing.tools.ActorTestingTools
import org.scalatest.mock.MockitoSugar
import model.Project
import scalax.file.Path
import service.build.MainBuilder.ProjectBuilderFactory
import model.reactive.event.EventProducer

class MainBuilderTest extends TestKit(ActorSystem("MainBuilderTest"))
    with FunSuiteLike with Matchers with BeforeAndAfterAll
    with BeforeAndAfter with ActorTestingTools with MockitoSugar {

    override def afterAll() { system.shutdown() }

    after { closeDummyActors(MainBuilder.name) }

    test("when receive LaunchBuild(project) should send LaunchBuild(project) to ProjectBuilder actor") {
        val projectBuilderProbe = TestProbe()

        val project = Project("project", "gitUrl", Path("thePath"))
        val stubEventProducer = mock[EventProducer[ProjectBuildEvent]]

        val underTest = TestActorRef(new MainBuilder(stubFactory(projectBuilderProbe)), MainBuilder.name)

        // When
        underTest ! LaunchBuild(project, stubEventProducer)

        // Then
        projectBuilderProbe expectMsg LaunchProjectBuild(project, stubEventProducer)
    }

    // TODO le test a-t-il un intérêt ? J'ai l'impression qu'on teste seulement la stubsFactoryForTwo
    ignore("when receive more than one LaunchBuild(projectN) should send each message to different ProjectBuilder actor") {
        val projectBuilderProbe1 = TestProbe()
        val projectBuilderProbe2 = TestProbe()

        val underTest = TestActorRef(new MainBuilder(stubsFactoryForTwo(projectBuilderProbe1, projectBuilderProbe2)), MainBuilder.name)

        val message1 = LaunchBuild(Project("project1", "gitUrl", Path("thePath")), mock[EventProducer[ProjectBuildEvent]])
        val message2 = LaunchBuild(Project("project2", "gitUrl", Path("thePath")), mock[EventProducer[ProjectBuildEvent]])

        // When
        underTest ! message1
        underTest ! message2

        // Then
        projectBuilderProbe1 expectMsg message1
        projectBuilderProbe2 expectMsg message2
    }

    ignore("if limit of ProjectBuilder are reached") {}

    private def stubFactory(probe: TestProbe): ProjectBuilderFactory =
        (_: String, _: ActorRefFactory) => new ProjectBuilder(probe.ref)

    private def stubsFactoryForTwo(probe1: TestProbe, probe2: TestProbe): ProjectBuilderFactory =
        (name: String, _: ActorRefFactory) =>
            if(name startsWith "project1") new ProjectBuilder(probe1.ref)
            else if(name startsWith "project2") new ProjectBuilder(probe2.ref)
            else throw new IllegalArgumentException("WTF")
}