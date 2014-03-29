package service.build

import akka.testkit.{TestProbe, TestActorRef, TestKit}
import akka.actor.{ActorRefFactory, ActorSystem}
import org.scalatest.{BeforeAndAfter, Matchers, BeforeAndAfterAll, FunSuiteLike}
import testing.tools.ActorTestingTools
import org.scalatest.mock.MockitoSugar
import model.Project
import scalax.file.Path
import service.build.MainBuilder.ProjectBuilderFactory
import model.reactive.event.{ProjectBuildEvent, EventProducer}

class MainBuilderTest extends TestKit(ActorSystem("MainBuilderTest"))
    with FunSuiteLike with Matchers with BeforeAndAfterAll
    with BeforeAndAfter with ActorTestingTools with MockitoSugar {

    override def afterAll() { system.shutdown() }

    after { closeDummyActors(MainBuilder.name) }

    test("when receive LaunchBuild(project) should send LaunchBuild(project) to ProjectBuilder actor") {
        val projectBuilderProbe = TestProbe()

        val project = Project("project", "gitUrl")
        val stubEventProducer = mock[EventProducer[ProjectBuildEvent]]

        val underTest = TestActorRef(new MainBuilder(stubFactory(projectBuilderProbe)), MainBuilder.name)

        // When
        underTest ! LaunchBuild(project, stubEventProducer)

        // Then
        projectBuilderProbe expectMsg LaunchProjectBuild(project, stubEventProducer)
    }

    // TODO le test a-t-il un intérêt ? J'ai l'impression qu'on teste seulement la stubsFactoryForTwo
    test("when receive more than one LaunchBuild(projectN) should send each message to different ProjectBuilder actor") {
        val projectBuilderProbe1 = TestProbe()
        val projectBuilderProbe2 = TestProbe()

        val eventProducerStub = mock[EventProducer[ProjectBuildEvent]]

        val underTest = TestActorRef(new MainBuilder(stubsFactoryForTwo(projectBuilderProbe1, projectBuilderProbe2)), MainBuilder.name)

        // When
        underTest ! LaunchBuild(Project("project1", "gitUrl"), eventProducerStub)
        underTest ! LaunchBuild(Project("project2", "gitUrl"), eventProducerStub)

        // Then
        projectBuilderProbe1 expectMsg LaunchProjectBuild(Project("project1", "gitUrl"), eventProducerStub)
        projectBuilderProbe2 expectMsg LaunchProjectBuild(Project("project2", "gitUrl"), eventProducerStub)
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