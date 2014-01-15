package service.build

import akka.actor._

case object CheckstyleSubBuilderName extends SubBuilderName

class CheckstyleSubBuilder(val ref: ActorRef) extends SubBuilder {
    def this(context: ActorRefFactory) = this(context.actorOf(CheckstyleSubBuilder.props, CheckstyleSubBuilder.name))
}

object CheckstyleSubBuilder {
    val name = "checkstyleSubBuilder"
    def props = Props[CheckstyleSubBuilderActor]
}

class CheckstyleSubBuilderActor extends Actor with ActorLogging {

    def receive = ???
}