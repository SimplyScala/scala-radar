package service.engine

import model.ScalaProjectMetaDatas
import scalax.file.Path

trait ScalaProjectParser {
    def produceScalaProjectMetadatas(files: Set[Path]): ScalaProjectMetaDatas
}

object ScalaProjectParser extends ScalaProjectParser {
    def produceScalaProjectMetadatas(files: Set[Path]): ScalaProjectMetaDatas = {
        val filesNumber = files.size

        // TODO produce the two metadatas in once
        val fileMetadatas = files.toList.map { path =>
            val maybeFile = path.fileOption map(io.Source.fromFile(_) getLines() toIterable)
            maybeFile.map(ScalaFileParser.produceFileMetadatas(_)).get
        }

        val scalaMetadatas = files.toList.map { path =>
            val maybeFile = path.fileOption map(io.Source.fromFile(_) getLines() toIterable)
            maybeFile.map(ScalaFileParser.produceScalaMetadatas(_)).get
        }

        ScalaProjectMetaDatas(filesNumber, fileMetadatas, scalaMetadatas)
    }
}