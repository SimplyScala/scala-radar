package model

import scalax.file.Path
import org.joda.time.DateTime

// TODO Path is unused so delete it
// TODO add builder app (use enum : APP_BUILDER => MAVEN, SBT, PLAY)
case class Project(name: String, gitUrl: String, path: Path) {
    def toBuild(startDate: DateTime = DateTime.now()): Build = Build(s"$name-${startDate.getMillis}", startDate, this)
}

// TODO Add Build path instead of Project Path
// TODO buildId is not useful here
case class Build(buildId: String, startDate: DateTime, project: Project) {
    def toSuccessfulBuild(endDate: DateTime = DateTime.now()): SuccessfulBuild =
        SuccessfulBuild(buildId, startDate.getMillis(), endDate.getMillis(), project.name, project.gitUrl, project.path.path)
}

//TODO use other constructeur to build buildId from startDate + projectName
case class SuccessfulBuild(buildId: String,
                           startDate: Long,
                           endDate: Long,
                           projectName: String,
                           projectGitUrl: String,
                           projectPath: String)
