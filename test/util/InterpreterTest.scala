package util

import org.scalatest.{Matchers, FunSuite}

class InterpreterTest extends FunSuite with Matchers {

  test("should interpret scala val on the fly") {
    val applicationPath = Interpreter.interpretAsStringVal("new java.io.File(\".\").getAbsolutePath()")
    applicationPath should include ("scala-radar")
  }
}
