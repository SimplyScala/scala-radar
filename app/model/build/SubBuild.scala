package model.build

import model.Build
import model.reactive.event.{CheckstyleDone, ScctDone, ProjectBuildEvent}

sealed trait SubBuild {
    def build: Build
    def toBuildEvent: ProjectBuildEvent
}
sealed case class TestCodeCoverageBuild(build: Build) extends SubBuild {
    def toBuildEvent: ProjectBuildEvent = ScctDone(build.project)
}
sealed case class CheckstyleBuild(build: Build) extends SubBuild {
    def toBuildEvent: ProjectBuildEvent = CheckstyleDone(build.project)
}