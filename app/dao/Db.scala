package dao

import sorm._
import model.SuccessfulBuild

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
    implicit val db: sorm.Instance
}

trait ProdDatabase extends Database {
    implicit val db = ProdDb
}
