package dao

import sorm._
import scala.slick.driver.JdbcDriver._
import model.SuccessfulBuild
import scala.slick.driver.HsqldbDriver.simple._

class Db(entities: Traversable[Entity] = Set(Entity[SuccessfulBuild]()),
         url: String = "jdbc:hsqldb:file:/Users/ugobourdon/test/db/test")
    extends Instance(
        entities = entities,
        url = url,    // TODO user.home or scala-radar.home
        user = "SA",
        password = "",
        initMode = InitMode.Create,
        poolSize = 1
    )

object ProdDb extends Instance (
    entities = Set(Entity[SuccessfulBuild]()),
    url = "jdbc:hsqldb:file:/Users/ugobourdon/test/db/test",
    user = "SA",
    password = "",
    initMode = InitMode.Create,
    poolSize = 1
)

trait Database {
    implicit def db: Backend#DatabaseDef
}

trait ProdDatabase extends Database { // TODO thread safe ??? + connection pool ? + init mode create ??
    implicit val db: Backend#DatabaseDef = Database.forURL(url = "jdbc:hsqldb:file:/Users/ugobourdon/test/db/test", user = "SA")
}
