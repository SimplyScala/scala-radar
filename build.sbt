name := "scala-radar"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache
)     

play.Project.playScalaSettings

ScctPlugin.instrumentSettings

ScctPlugin.scctExcludePackages in ScctPlugin.ScctTest := "controllers.javascript,controllers.ref,controllers.ReverseAssets*,controllers.ReverseApplication*,views*"
