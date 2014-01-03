package util

import org.scalatest.{Matchers, FunSuite}

class RichStringTest extends FunSuite with Matchers with RichString {
    test("richString.isBlank") {
        "".isBlank should be (true)
        " ".isBlank should be (true)
        "   ".isBlank should be (true)
    }
}