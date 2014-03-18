package model

import scalax.file.Path
import org.joda.time.DateTime

// case class Project(name: String, url: String)
case class Project(name: String, url: String, path: Path) {
    def toBuild(startDate: DateTime = DateTime.now()): Build = Build(s"$name-${startDate.getMillis}", startDate, this)
}

// case class Build(buildId: String, startDate: DateTime, project: Project, path: Path)
case class Build(buildId: String, startDate: DateTime, project: Project) {
    def toSuccessfulBuild(endDate: DateTime = DateTime.now()): SuccessfulBuild =
        SuccessfulBuild(buildId, startDate.getMillis(), endDate.getMillis(), project.name, project.url, project.path.path)
}

// SuccessfulBuild(buildId: String, startDate: Long, endDate: Long, projectPath: String)
case class SuccessfulBuild(buildId: String, startDate: Long, endDate: Long, projectName: String, projectUrl: String, projectPath: String)