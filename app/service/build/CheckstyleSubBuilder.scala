package service.build

import akka.actor._
import scalax.file.Path
import model.Build

case object CheckstyleSubBuilderName extends SubBuilderName

class CheckstyleSubBuilder(val ref: ActorRef) extends SubBuilder {
    def this(context: ActorRefFactory) = this(context.actorOf(CheckstyleSubBuilder.props, CheckstyleSubBuilder.name))
}

object CheckstyleSubBuilder {
    val name = "checkstyleSubBuilder"
    def props = Props[CheckstyleSubBuilderActor]
}

class CheckstyleSubBuilderActor extends Actor with ActorLogging {
    import scala.sys.process._

    def receive = {
        case LaunchSubBuild(build) =>
            val scalastyleDir = "/Users/ugobourdon/Dev/apps/scalastyle-batch_2.10-0.3.2"
            val scalastyleJar = s"$scalastyleDir/scalastyle-batch_2.10.jar"
            val scalastyleConfig = s"$scalastyleDir/scalastyle_config.xml"

            val scalastyleReportDir = Path.fromString(build.project.path.path + "/target/scalastyle-report").createDirectory()

            val resultDir = s"${scalastyleReportDir.path}/${build.project.name}_report.xml"

            val appUnderTest = build.project.path.path

            val result = s"java -jar $scalastyleJar --xmlOutput $resultDir --config $scalastyleConfig $appUnderTest/app" !!

            context.parent ! SubBuildDone(CheckstyleBuild(build))
    }
}