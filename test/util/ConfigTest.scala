package util

import org.scalatest.{Matchers, FunSuite}

class ConfigTest extends FunSuite with Matchers {

  test("should retrieve application path from project config") {
    Config.getApplicationPath() should include ("scala-radar")
  }

}
