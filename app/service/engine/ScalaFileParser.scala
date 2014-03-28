package service.engine

import util.RichString
import model.engine.{ScalaMetadatas, ScalaFileMetadatas}

object ScalaFileParser extends RichString {
    // TODO use FileLines label instead of Iterable[String]
    def produceFileMetadatas(fileLines: Iterable[String]): ScalaFileMetadatas = {
        val linesNumber = fileLines.size
        val blankLinesNumber = fileLines.count( _.isBlank )
        val simpleCommentLinesNumber = fileLines.count( _.trim.startsWith("//") )
        val multiLineCommentLineNumber = {
            var count = 0
            var isMultiComment = false
            for(line <- fileLines) {
                if(isMultiComment) count += 1
                if(line.trim.startsWith("/*")) { count += 1; isMultiComment = true }
                if(line.trim.endsWith("*/")) isMultiComment = false
            }
            // TODO refaire le calcul de manière fonctionnelle

            count
        }
        val headerLine = fileLines.count { line => line.trim.startsWith("import") || line.trim.startsWith("package") }
        val codeLine = linesNumber - blankLinesNumber - simpleCommentLinesNumber - multiLineCommentLineNumber - headerLine

        ScalaFileMetadatas(linesNumber, blankLinesNumber, simpleCommentLinesNumber + multiLineCommentLineNumber, headerLine, codeLine)
    }

    def produceScalaMetadatas(fileLines: Iterable[String]): ScalaMetadatas = {
        // TODO sealed case class, sealed class
        // TODO case object

        val classNumber = countOccurenceWithoutMultiLineComment(line => line.trim.startsWith("class"), fileLines)
        val caseClassNumber = countOccurenceWithoutMultiLineComment(line => line.trim.startsWith("case class"), fileLines)
        val traitNumber = countOccurenceWithoutMultiLineComment(line => line.trim.startsWith("trait") || line.trim.startsWith("sealed trait"), fileLines)
        val objectNumber = countOccurenceWithoutMultiLineComment(line => line.trim.startsWith("object"), fileLines)

        ScalaMetadatas(classNumber, caseClassNumber, traitNumber, objectNumber)
    }

    private def countOccurenceWithoutMultiLineComment(testOccurence: String => Boolean, fileLines: Iterable[String]): Int = {
        var isMultiComment = false
        var count = 0
        for(line <- fileLines) {
            if(line.trim.startsWith("/*")) isMultiComment = true
            if(line.trim.endsWith("*/")) isMultiComment = false
            if(!isMultiComment && testOccurence(line)) count += 1
        }

        count
    }
}