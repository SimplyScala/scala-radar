import java.io.File
import org.joda.time.DateTime
import org.scalatest.{Matchers, FunSuite}
import scalax.file.Path

class CmdLineProcessTest extends FunSuite with Matchers {

    //file:////Users/ugobourdon/Documents/SCALA/scaladocs/scala-docs-2.10.1/index.html#scala.sys.process.package

    ignore("test ls cmd") {
        import scala.sys.process._

        // This uses ! to get the exit code
        val ls_cmd_withExitCode = Seq("ls") !

        // This uses !! to get the whole result as a string
        //val cd_cmd = "cd /Users/ugobourdon/" !!
        //val ls_cmd = "ls" !!

        val lsAfterCd_cmd = "cd /Users/ugobourdon/ && ls" !!

        //val bash = "/Users/ugobourdon/test.sh"

        //println(ls_cmd_withExitCode)
        //println(ls_cmd)
        //println(lsAfterCd_cmd)
        //println(bash)
    }

    ignore("git clone cmd + play test") {
        import scala.sys.process._

        // bloquant
        "rm -rf /Users/ugobourdon/test/scala-radar" !!

        // bloquant
        val result = "git clone git@github.com:SimplyScala/scala-radar.git /Users/ugobourdon/test/scala-radar" !!

        val play_cmd = "/Users/ugobourdon/Dev/apps/play-2.2.1/play"
        val result_2 = Process(Seq(play_cmd, "test"), Option(new File("/Users/ugobourdon/test/scala-radar"))).lines

        result_2.foreach(println)
    }

    ignore("spike2") {
        import scala.sys.process._
        // commit ea1c86e OK
        // commit e82f443
        val t = Option(new File("/Users/ugobourdon/Dev/Projects/ScalaQuality/scala-radar/public/builds/scala-radar/scala-radar-1395323701715"))
        //val t = Option(new File("/Users/ugobourdon/test/scala-radar/scala-radar-1395331169487"))
        //val t = Option(new File("/Users/ugobourdon/test/scala-radar-1395167987078"))
        val play_cmd = "/Users/ugobourdon/Dev/apps/play-2.2.1/play"
        val result = Process(Seq(play_cmd, "scct:test"), t) !!

        println(result)
    }

    ignore("scala check style") {
        import scala.sys.process._

        val scalastyleDir = "/Users/ugobourdon/Dev/apps/scalastyle-batch_2.10-0.3.2"
        val scalastyleJar = s"$scalastyleDir/scalastyle-batch_2.10.jar"
        val scalastyleConfig = s"$scalastyleDir/scalastyle_config.xml"

        val resultDir = "/Users/ugobourdon/test/scala-radar_style.xml"

        val appUnderTest = "/Users/ugobourdon/Dev/Projects/ScalaQuality/scala-radar/app"
        //val appUnderTest = "/Users/ugobourdon/test/scala-radar-1391359829433/app"

        val result = s"java -jar $scalastyleJar --xmlOutput $resultDir --config $scalastyleConfig $appUnderTest" !!
    }

    ignore("spike sorm with hsqldb") {
        /*case class Toto(titi: String)  // case class must be in other file

        import sorm._
        object Db extends Instance(
            entities = Set(Entity[Toto]()),
            url = "jdbc:hsqldb:file:/Users/ugobourdon/test/db/test",
//            url = "jdbc:h2:mem:test",
            user = "SA",
            password = "",
            initMode = InitMode.Create
        )

        Db.query[Toto].count() shouldBe 0
        Db.save(Toto("titi"))
        Db.query[Toto].count() shouldBe 1*/
    }

    ignore("toto") {
        println(new File("public/builds").getAbsolutePath)
        println(Path.fromString("public/builds").path)
    }
}