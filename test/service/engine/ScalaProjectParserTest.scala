package service.engine

import org.scalatest.{Matchers, FunSuite}
import model.{ScalaMetadatas, ScalaFileMetadatas, ScalaProjectMetaDatas}
import scalax.file.ImplicitConversions._

class ScalaProjectParserTest extends FunSuite with Matchers {

    // http://jesseeichar.github.io/scala-io-doc/0.4.2/index.html#!/file/standard_examples
    test("test file api with scala-io") {
        import scalax.file.Path
        import scalax.file.ImplicitConversions._
        import scalax.file.PathSet

        // find all .scala files in a sourcePath or one of its sub-directories
        val sourcePath = "/Users/ugobourdon/Dev/Projects/ScalaQuality/scala-radar/app"
        val scalaFiles: PathSet[Path] = sourcePath ** "*.scala"
        //scalaFiles.toSet.foreach(println)
    }

    test("test reduce of total line number") {
        val metadatas = List(ScalaFileMetadatas(1, 0, 0, 0, 0), ScalaFileMetadatas(1, 0, 0, 0, 0))
        ScalaProjectMetaDatas(3, metadatas, Nil) shouldBe ScalaProjectMetaDatas(3, 0, 0, 0, 0, 2, 0, 0, 0, 0)
    }

    test("test reduce of blank line number") {
        val metadatas = List(ScalaFileMetadatas(0, 1, 0, 0, 0), ScalaFileMetadatas(0, 1, 0, 0, 0))
        ScalaProjectMetaDatas(3, metadatas, Nil) shouldBe ScalaProjectMetaDatas(3, 0, 0, 0, 0, 0, 2, 0, 0, 0)
    }

    test("test reduce of comment line number") {
        val metadatas = List(ScalaFileMetadatas(0, 0, 1, 0, 0), ScalaFileMetadatas(0, 0, 1, 0, 0))
        ScalaProjectMetaDatas(3, metadatas, Nil) shouldBe ScalaProjectMetaDatas(3, 0, 0, 0, 0, 0, 0, 2, 0, 0)
    }

    test("test reduce of header line number") {
        val metadatas = List(ScalaFileMetadatas(0, 0, 0, 0, 1), ScalaFileMetadatas(0, 0, 0, 0, 1))
        ScalaProjectMetaDatas(3, metadatas, Nil) shouldBe ScalaProjectMetaDatas(3, 0, 0, 0, 0, 0, 0, 0, 0, 2)
    }

    test("test reduce of code line number") {
        val metadatas = List(ScalaFileMetadatas(0, 0, 0, 1, 0), ScalaFileMetadatas(0, 0, 0, 1, 0))
        ScalaProjectMetaDatas(3, metadatas, Nil) shouldBe ScalaProjectMetaDatas(3, 0, 0, 0, 0, 0, 0, 0, 2, 0)
    }

    test("test reduce of class number") {
        val metadatas = List(ScalaMetadatas(1, 0, 0, 0), ScalaMetadatas(1, 0, 0, 0))
        ScalaProjectMetaDatas(3, Nil, metadatas) shouldBe ScalaProjectMetaDatas(3, 2, 0, 0, 0, 0, 0, 0, 0, 0)
    }

    test("test reduce of case class number") {
        val metadatas = List(ScalaMetadatas(0, 1, 0, 0), ScalaMetadatas(0, 1, 0, 0))
        ScalaProjectMetaDatas(3, Nil, metadatas) shouldBe ScalaProjectMetaDatas(3, 0, 2, 0, 0, 0, 0, 0, 0, 0)
    }

    test("test reduce of trait number") {
        val metadatas = List(ScalaMetadatas(0, 0, 1, 0), ScalaMetadatas(0, 0, 1, 0))
        ScalaProjectMetaDatas(3, Nil, metadatas) shouldBe ScalaProjectMetaDatas(3, 0, 0, 2, 0, 0, 0, 0, 0, 0)
    }

    test("test reduce of object number") {
        val metadatas = List(ScalaMetadatas(0, 0, 0, 1), ScalaMetadatas(0, 0, 0, 1))
        ScalaProjectMetaDatas(3, Nil, metadatas) shouldBe ScalaProjectMetaDatas(3, 0, 0, 0, 2, 0, 0, 0, 0, 0)
    }

    test("[integration] should count class number") {
        val scalaFile = ("test" \ "resources" \ "stubedProject" \ "simplePackage" * "*.scala").toSet
        ScalaProjectParser.produceScalaProjectMetadatas(scalaFile) shouldBe ScalaProjectMetaDatas(3, 1, 1, 1, 1, 22, 7, 4, 4, 7)
    }
}