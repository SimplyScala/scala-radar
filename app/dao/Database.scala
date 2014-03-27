package dao

import scala.slick.driver.JdbcDriver._
import scala.slick.driver.HsqldbDriver.simple._

trait Database { implicit def db: Backend#DatabaseDef }

trait ProdDatabase extends Database { // TODO thread safe ??? + connection pool ? + init mode create ??
    implicit val db: Backend#DatabaseDef = Database.forURL(url = "jdbc:hsqldb:file:/Users/ugobourdon/test/db/test", user = "SA")
}