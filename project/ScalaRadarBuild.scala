import sbt._
import sbt.Keys._

object ScalaRadarBuild extends Build {
    val appName = "scala-radar"
    val appVersion = "1.0-SNAPSHOT"

    val appDependencies = Seq(
        "com.github.scala-incubator.io" %% "scala-io-core"  % "0.4.2",
        "com.github.scala-incubator.io" %% "scala-io-file"  % "0.4.2",
        "joda-time"                     %  "joda-time"      % "2.3",
        "org.hsqldb"                    %  "hsqldb"         % "2.3.2",
        "org.sorm-framework"            %  "sorm"           % "0.3.9",
        /*"com.typesafe.slick"            %% "slick"          % "2.0.0",
        "org.slf4j"                     % "slf4j-nop"       % "1.6.4",*/
        "org.squeryl"                   %% "squeryl"        % "0.9.5-6",
        "com.h2database"                %  "h2"              % "1.2.127",
        "org.scalatest"                 %% "scalatest"      % "2.0"      % "test",
        "com.typesafe.akka"             %% "akka-testkit"   % "2.2.1"    % "test",
        "org.mockito"                   %  "mockito-all"    % "1.9.5"    % "test"
    )

    val main = play.Project(appName, appVersion, appDependencies).settings()
}