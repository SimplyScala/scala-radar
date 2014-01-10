package service.build

import akka.actor._

class CheckstyleSubBuilder(val ref: ActorRef) {
    def this(system: ActorSystem) = this(system.actorOf(CheckstyleSubBuilder.props, CheckstyleSubBuilder.name))
}

object CheckstyleSubBuilder {
    val name = "checkstyleSubBuilder"
    def props = Props[CheckstyleSubBuilderActor]
}

class CheckstyleSubBuilderActor extends Actor with ActorLogging {

    def receive = ???
}