package service.build

import akka.actor._

class TestCodeCoverageSubBuilder(val ref: ActorRef) {
    def this(system: ActorSystem) = this(system.actorOf(TestCodeCoverageSubBuilder.props, TestCodeCoverageSubBuilder.name))
}

object TestCodeCoverageSubBuilder {
    val name = "testCodeCoverageSubBuilder"
    def props = Props[TestCodeCoverageSubBuilderActor]
}

class TestCodeCoverageSubBuilderActor extends Actor with ActorLogging {

    def receive = ???
}