package util

trait RichString {
    implicit def string2RichString(string: String): RichString = RichString(string)

    case class RichString(string: String) {
        def isBlank: Boolean = string.trim.isEmpty
    }
}