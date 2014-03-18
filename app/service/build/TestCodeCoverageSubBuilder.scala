package service.build

import akka.actor._
import java.io.File
import scala.sys.process.Process

case object TestCodeCoverageSubBuilderName extends SubBuilderName

class TestCodeCoverageSubBuilder(val ref: ActorRef) extends SubBuilder {
    def this(context: ActorRefFactory) = this(context.actorOf(TestCodeCoverageSubBuilder.props, TestCodeCoverageSubBuilder.name))
}

object TestCodeCoverageSubBuilder {
    val name = "testCodeCoverageSubBuilder"
    def props = Props[TestCodeCoverageSubBuilderActor]
}

class TestCodeCoverageSubBuilderActor extends Actor with ActorLogging {

    def receive = {
        case LaunchSubBuild(build) =>
            val play_cmd = "/Users/ugobourdon/Dev/apps/play-2.2.1/play"
            val logs = Process(Seq(play_cmd, "scct:test"), build.project.path.fileOption) !!

            //log.info(logs)

            context.parent ! SubBuildDone(TestCodeCoverageBuild(build))
    }
}