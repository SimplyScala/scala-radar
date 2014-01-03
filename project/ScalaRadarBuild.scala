import sbt._
import sbt.Keys._

object ScalaRadarBuild extends Build {
    val appName = "scala-radar"
    val appVersion = "1.0-SNAPSHOT"

    val appDependencies = Seq(
        "org.scalatest" %% "scalatest" % "2.0" % "test",
        "com.github.scala-incubator.io" %% "scala-io-core" % "0.4.2",
        "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.2"
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(

        //testOptions in Test := Nil, //to run scalatest in play2 console arghhhh!!!

        // available test resources in play2 classpath
        unmanagedClasspath in Test <+= (baseDirectory) map {
            bd => Attributed.blank(bd / "test")
        }
    )
}