package service.build

import akka.actor._
import scala.sys.process.Process

case object TestCodeCoverageSubBuilderName extends SubBuilderName

class TestCodeCoverageSubBuilder(val ref: ActorRef) extends SubBuilder {
    def this(context: ActorRefFactory) =
        this(context.actorOf(TestCodeCoverageSubBuilder.props, TestCodeCoverageSubBuilder.name))
}

object TestCodeCoverageSubBuilder {
    val name = "testCodeCoverageSubBuilder"
    def props = Props[TestCodeCoverageSubBuilderActor]
}

class TestCodeCoverageSubBuilderActor extends Actor with ActorLogging {

    // TODO use BashExecutor.playCmd
    // TODO avoir l'info de combien prend en temps l'éxecution des tests
    def receive = {
        case LaunchSubBuild(build) =>
            val play_cmd = "/Users/ugobourdon/Dev/apps/play-2.2.1/play"
            val logs =
                try { Process(Seq(play_cmd, "clean scct:test"), build.project.path.fileOption) !! }
                catch { case e: Throwable => log.error(e, "toto"); "error" }
            // TODO renvoyé KO si KO : context.parent ! SubBuildKO(TestCodeCoverageBuild(build))

            //log.info(logs)

            context.parent ! SubBuildDone(TestCodeCoverageBuild(build))
    }
}