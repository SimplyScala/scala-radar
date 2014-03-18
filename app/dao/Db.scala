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
        initMode = InitMode.Create
    )