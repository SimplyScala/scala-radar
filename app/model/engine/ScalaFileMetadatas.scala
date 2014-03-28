package model.engine

case class ScalaFileMetadatas(totalLineNumber: Int,
                              blankLineNumber: Int,
                              commentLineNumber: Int,
                              headerLineNumber: Int,
                              codeLineNumber: Int)

// TODO add def number
// TODO add private def number
// TODO add statements param
case class ScalaMetadatas(classNumber: Int, caseClassNumber: Int, traitNumber: Int, objectNumber: Int)

object ScalaProjectMetaDatas {
    def apply(fileNumber: Int, fileMetadatas: Seq[ScalaFileMetadatas], scalaMetadatas: Seq[ScalaMetadatas]): ScalaProjectMetaDatas =
        ScalaProjectMetaDatas(
            fileNumber = fileNumber,
            classNumber = if(!scalaMetadatas.isEmpty) scalaMetadatas.map(_.classNumber).reduce(_ + _) else 0,
            caseClassNumber = if(!scalaMetadatas.isEmpty) scalaMetadatas.map(_.caseClassNumber).reduce(_ + _) else 0,
            traitNumber = if(!scalaMetadatas.isEmpty) scalaMetadatas.map(_.traitNumber).reduce(_ + _) else 0,
            objectNumber = if(!scalaMetadatas.isEmpty) scalaMetadatas.map(_.objectNumber).reduce(_ + _) else 0,
            totalLineNumber = if(!fileMetadatas.isEmpty) fileMetadatas.map(_.totalLineNumber).reduce(_ + _) else 0,
            blankLineNumber = if(!fileMetadatas.isEmpty) fileMetadatas.map(_.blankLineNumber).reduce(_ + _) else 0,
            commentLineNumber = if(!fileMetadatas.isEmpty) fileMetadatas.map(_.commentLineNumber).reduce(_ + _) else 0,
            headerLineNumber = if(!fileMetadatas.isEmpty) fileMetadatas.map(_.headerLineNumber).reduce(_ + _) else 0,
            codeLineNumber = if(!fileMetadatas.isEmpty) fileMetadatas.map(_.codeLineNumber).reduce(_ + _) else 0
        )
}

// TODO add package number
case class ScalaProjectMetaDatas(fileNumber: Int,
                                 classNumber: Int,
                                 caseClassNumber: Int,
                                 traitNumber: Int,
                                 objectNumber: Int,
                                 totalLineNumber: Int,
                                 blankLineNumber: Int,
                                 commentLineNumber: Int,
                                 headerLineNumber: Int,
                                 codeLineNumber: Int)