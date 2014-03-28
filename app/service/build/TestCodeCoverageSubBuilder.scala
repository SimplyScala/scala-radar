package service.build

import akka.actor._
import scala.util.{Failure, Success}

case object TestCodeCoverageSubBuilderName extends SubBuilderName

class TestCodeCoverageSubBuilder(val ref: ActorRef) extends SubBuilder {
    def this(context: ActorRefFactory) =
        this(context.actorOf(TestCodeCoverageSubBuilder.props(), TestCodeCoverageSubBuilder.name))
}

object TestCodeCoverageSubBuilder {
    val name = "testCodeCoverageSubBuilder"
    def props(bashExecutor: BashExecutor = BashExecutor) = Props(new TestCodeCoverageSubBuilderActor(bashExecutor))
}

class TestCodeCoverageSubBuilderActor(bashExecutor: BashExecutor) extends Actor with ActorLogging {

    // TODO avoir l'info de combien prend en temps l'éxecution des tests
    def receive = {
        case LaunchSubBuild(build) => bashExecutor.executeScctTestCmd(build) match {
                case Success(log) => context.parent ! SubBuildDone(TestCodeCoverageBuild(build))
                case Failure(e) => context.parent ! SubBuildFailed(TestCodeCoverageBuild(build))
            }
            // TODO qui log le résultat : cet acteur ou l'acteur parent ?
    }
}