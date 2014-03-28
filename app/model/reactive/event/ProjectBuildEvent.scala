package model.reactive.event

import model.Project

sealed trait ProjectBuildEvent { def eventName: String; def project: Project }

sealed case class ProjectCloned(project: Project) extends ProjectBuildEvent { def eventName: String = "projectCloned" }

sealed case class ScctDone(project: Project) extends ProjectBuildEvent { def eventName: String = "scctDone" }

sealed case class ScctFailed(project: Project) extends ProjectBuildEvent { def eventName: String = "scctFailed" }

sealed case class CheckstyleDone(project: Project) extends ProjectBuildEvent { def eventName: String = "checkstyleDone" }

sealed case class BuildDone(project: Project) extends ProjectBuildEvent { def eventName: String = "buildDone" }