import sbt._
import sbt.Keys._

object ScalaRadarBuild extends Build {
    val appName = "scala-radar"
    val appVersion = "1.0-SNAPSHOT"

    val appDependencies = Seq(
        "com.github.scala-incubator.io" %% "scala-io-core" % "0.4.2",
        "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.2",
        "joda-time"                     %  "joda-time"     % "2.3",
        "org.scalatest"                 %% "scalatest"     % "2.0"      % "test",
        "com.typesafe.akka"             %% "akka-testkit"  % "2.2.1"    % "test",
        "org.mockito"                   %  "mockito-all"   % "1.9.5"    % "test",
        "org.scala-lang"                %  "scala-compiler" % "2.10.3"
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(

        // available test resources in play2 classpath
        unmanagedClasspath in Test <+= (baseDirectory) map { bd => Attributed.blank(bd / "test") }
    )
}