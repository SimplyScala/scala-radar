package service.build

import scalax.file.Path
import scala.sys.process._
import model.Project
import org.joda.time.DateTime
import java.io.File


trait BashExecutor {
    def gitCloneProject(project: Project, startDate: DateTime): Path
}

// TODO use Monad IO to perform this things ???
// TODO comment tester unitairement ce truc ???
    // file system virtuel, mais il me faut un git installé aussi, et une url git locale ?
object BashExecutor extends BashExecutor {
    // TODO git clone project via project.url dans scalaradarTmpHome/projectId-timestampValue/
    def gitCloneProject(project: Project, startDate: DateTime): Path = {
        val path = new File(s"public/builds/${project.name}/${project.name}-${startDate.getMillis}").getAbsolutePath  // TODO use relative Url /public/builds/...
        val projectBuildDirectoryPath = Path.fromString(path)
        val result = s"git clone ${project.url} ${projectBuildDirectoryPath.path}" !!

        // TODO projectBuildDirectoryPath doit être externaliser en dehors de la méthode et injecté par paramètre

        projectBuildDirectoryPath
    }

    // TODO test if build is OK
    def launchPlayTestCmd(project: Project): Unit = {
        val play_cmd = "/Users/ugobourdon/Dev/apps/play-2.2.1/play"
        val result_2 = Process(Seq(play_cmd, "test"), project.path.fileOption) !!
    }
}