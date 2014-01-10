package service.build

import scalax.file.Path
import scala.sys.process._


trait BashExecutor {
    def gitCloneProject(gitUrl: String): Path
}

object BashExecutor extends BashExecutor {
    // TODO git clone project via project.url dans scalaradarTmpHome/projectId-timestampValue/
    def gitCloneProject(gitUrl: String): Path = ???
    //val projectBuildDirectoryPath = Path.fromString(s"/Users/ugobourdon/test/${project.name}-${DateTime.now()}")
    //val result = s"git clone git@github.com:SimplyScala/scala-radar.git ${projectBuildDirectoryPath.path}" !!
}