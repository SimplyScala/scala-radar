package model.build

sealed trait SubBuilderName

case object TestCodeCoverageSubBuilderName extends SubBuilderName

case object CheckstyleSubBuilderName extends SubBuilderName
