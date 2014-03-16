package util

import scala.tools.nsc.interpreter.IMain
import java.io.File
import scala.tools.nsc.Settings

object Interpreter {

  def interpretAsStringVal(codeToInterpret: String) = {
    val settings = new Settings

    //Including scala-library in project is necessary in order to give to play framework this lib, play (through sbt) doesn't include scala-library in classpath
    settings.bootclasspath.value +=scala.tools.util.PathResolver.Environment.javaBootClassPath + File.pathSeparator + "lib/scala-library-2.10.3.jar"
    val interpreter = new IMain(settings){
      override protected def parentClassLoader = settings.getClass.getClassLoader()
    }

    interpreter.interpret("val resultVal=" + codeToInterpret)

    val resultVal = interpreter.valueOfTerm("resultVal").get.toString
    interpreter.close()
    resultVal
  }

}
