package model

import scalax.file.Path
import org.joda.time.DateTime

// TODO rajouter une référence au build en cours ???
case class Project(name: String, url: String, path: Path)

case class Build(id: String, startDate: DateTime)