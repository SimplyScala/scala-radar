package service.engine

import org.scalatest.{Matchers, FunSuite}
import model.{ScalaMetadatas, ScalaFileMetadatas}
import util.RichString

class ScalaFileParserTest extends FunSuite with Matchers with RichString {

    // ref : https://github.com/SonarCommunity/sonar-scala/blob/master/src/main/java/org/sonar/plugins/scala/language/Comment.java
    // TODO move file in test resources
    test("""[integration] should produce Scala file metadatas :
            - lines number
            - blank lines number
            - comment line number (// or /**/)
            - headerLine number (package & import)
            - code line number
        """) {
        val lines = io.Source.fromFile("public/resources/StubServer.scala").getLines().toIterable

        ScalaFileParser.produceFileMetadatas(lines) shouldBe ScalaFileMetadatas(105, 21, 8, 7, 69)
    }

    test("should count class number") {
        val lines = "  class ACompter()".split("\\n")
        ScalaFileParser.produceScalaMetadatas(lines) shouldBe ScalaMetadatas(1, 0, 0, 0)
    }

    test("should not count class number when under comment //") {
        val lines = "// class ANePasCompter".split("\\n")
        ScalaFileParser.produceScalaMetadatas(lines) shouldBe ScalaMetadatas(0, 0, 0, 0)
    }

    test("should not count occurence when under comment /**/") {
        val lines = """/*

           class ANePasCompter
           case class ANePasCompter
           trait ANePasCompter
           object ANePasCompter
          */""".split("\\n")

        ScalaFileParser.produceScalaMetadatas(lines) shouldBe ScalaMetadatas(0, 0, 0, 0)
    }

    test("should count case class number") {
        val lines = "  case class ACompter()".split("\\n")
        ScalaFileParser.produceScalaMetadatas(lines) shouldBe ScalaMetadatas(0, 1, 0, 0)
    }

    test("should count trait number") {
        val lines = "  trait ACompter()".split("\\n")
        ScalaFileParser.produceScalaMetadatas(lines) shouldBe ScalaMetadatas(0, 0, 1, 0)
    }

    test("should count sealed trait number") {
        val lines = "  sealed trait ACompter()".split("\\n")
        ScalaFileParser.produceScalaMetadatas(lines) shouldBe ScalaMetadatas(0, 0, 1, 0)
    }

    test("should count object number") {
        val lines = "  object ACompter()".split("\\n")
        ScalaFileParser.produceScalaMetadatas(lines) shouldBe ScalaMetadatas(0, 0, 0, 1)
    }

    // TODO move file in test resources
    test("""[integration] should produce Scala metadatas :
            - class number
            - case class number
            - trait number
            - object number
         """) {
        val lines = io.Source.fromFile("public/resources/StubServer.scala").getLines().toIterable
        ScalaFileParser.produceScalaMetadatas(lines) shouldBe ScalaMetadatas(1, 3, 1, 8)
    }
}