package service.build

import scalax.file.Path
import model.{Build, Project}
import org.joda.time.DateTime
import java.io.File
import service.build.BashExecutor.Logs
import scala.util.Try
import scala.sys.process._


trait BashExecutor {
    def gitCloneProject(project: Project, startDate: DateTime): Path
    def executeScctTestCmd(build: Build): Try[Logs]
}

// TODO use Monad IO to perform this things ???
// TODO comment tester unitairement ce truc ???
    // file system virtuel, mais il me faut un git installé aussi, et une url git locale ?
object BashExecutor extends BashExecutor {
    type Logs = String
    
    def gitCloneProject(project: Project, startDate: DateTime): Path = {
        val path = new File(s"public/builds/${project.name}/${project.name}-${startDate.getMillis}").getAbsolutePath
        val projectBuildDirectoryPath = Path.fromString(path)
        val result = s"git clone ${project.gitUrl} ${projectBuildDirectoryPath.path}" !!

        // TODO projectBuildDirectoryPath doit être externaliser en dehors de la méthode et injecté par paramètre

        projectBuildDirectoryPath
    }

    // TODO add builder app (use enum : APP_BUILDER => MAVEN, SBT, PLAY)
    def executeScctTestCmd(build: Build): Try[Logs] = {
        val play_cmd = "/Users/ugobourdon/Dev/apps/play-2.2.1/play"
        Try(Process(Seq(play_cmd, "scct:test"), build.path.fileOption) !!)
    }
}
