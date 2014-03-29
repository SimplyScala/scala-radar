package model

import scalax.file.Path
import org.joda.time.DateTime

// TODO add builder app (use enum : APP_BUILDER => MAVEN, SBT, PLAY) => ProjectConfig object instead
case class Project(name: String, gitUrl: String) {
    def toBuild(path: Path, startDate: DateTime = DateTime.now()): Build = Build(s"$name-${startDate.getMillis}", startDate, path, this)
}

// TODO buildId is not useful here
case class Build(buildId: String, startDate: DateTime, path: Path, project: Project) {
    def toSuccessfulBuild(endDate: DateTime = DateTime.now()): SuccessfulBuild =
        SuccessfulBuild(buildId, startDate.getMillis(), endDate.getMillis(), path.path, project.name, project.gitUrl)
}

//TODO use other constructeur to build buildId from startDate + projectName
case class SuccessfulBuild(buildId: String,
                           startDate: Long,
                           endDate: Long,
                           path: String,
                           projectName: String,
                           projectGitUrl: String)
