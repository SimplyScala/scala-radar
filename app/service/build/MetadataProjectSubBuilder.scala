package service.build

import akka.actor._

class MetadataProjectSubBuilder(val ref: ActorRef) {
    def this(system: ActorSystem) = this(system.actorOf(MetadataProjectSubBuilder.props, MetadataProjectSubBuilder.name))
}

object MetadataProjectSubBuilder {
    val name = "metadataProjectSubBuilder"
    def props = Props[MetadataProjectSubBuilderActor]
}

class MetadataProjectSubBuilderActor extends Actor with ActorLogging {

    def receive = ???
}