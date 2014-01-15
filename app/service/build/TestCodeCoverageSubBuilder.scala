package service.build

import akka.actor._

case object TestCodeCoverageSubBuilderName extends SubBuilderName

class TestCodeCoverageSubBuilder(val ref: ActorRef) extends SubBuilder {
    def this(context: ActorRefFactory) = this(context.actorOf(TestCodeCoverageSubBuilder.props, TestCodeCoverageSubBuilder.name))
}

object TestCodeCoverageSubBuilder {
    val name = "testCodeCoverageSubBuilder"
    def props = Props[TestCodeCoverageSubBuilderActor]
}

class TestCodeCoverageSubBuilderActor extends Actor with ActorLogging {

    def receive = ???
}