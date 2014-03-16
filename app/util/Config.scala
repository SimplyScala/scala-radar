package util

import com.typesafe.config.ConfigFactory

object Config {
  val conf = ConfigFactory.load("paths.conf")
  val applicationPath = Interpreter.interpretAsStringVal(conf.getString("app.path"))

  def getApplicationPath() = applicationPath
}
