package model

import scalax.file.Path

// TODO rajouetr une référence au build en cours ???
case class Project(name: String, url: String, path: Path)