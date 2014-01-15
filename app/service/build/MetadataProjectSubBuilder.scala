package service.build

import akka.actor._
import service.engine.ScalaProjectParser

case object MetadataProjectSubBuilderName extends SubBuilderName

class MetadataProjectSubBuilder(val ref: ActorRef) extends SubBuilder {
    def this(context: ActorRefFactory) = this(context.actorOf(MetadataProjectSubBuilder.props, MetadataProjectSubBuilder.name))
}

object MetadataProjectSubBuilder {
    val name = "metadataProjectSubBuilder"
    def props = Props[MetadataProjectSubBuilderActor]
}

class MetadataProjectSubBuilderActor(scalaProjectParser: ScalaProjectParser = ScalaProjectParser) extends Actor with ActorLogging {

    def receive = {
        case LaunchSubBuild(project) => ???
            // TODO ScalaProjectParser.produceScalaProjectMetadatas
    }
}