package dao

import org.scalatest.{Matchers, FunSuite}
import model.SuccessfulBuild

class DbTest extends FunSuite with Matchers {

    import scala.slick.driver.HsqldbDriver.simple._
    import dao.schema.ScalaRadarSchemas
    import scala.slick.driver.JdbcDriver

    test("test slick") {
        val build = SuccessfulBuild("id", 123L, 321L, "path", "name", "url")
        val build2 = SuccessfulBuild("id2", 123L, 421L, "path", "name", "url2")

        val db: JdbcDriver.Backend#DatabaseDef = Database.forURL(url = "jdbc:hsqldb:mem:test", user = "SA")

        db.withSession { implicit session =>
            ScalaRadarSchemas.builds.ddl.create

            ScalaRadarSchemas.builds.length.run shouldBe 0

            ScalaRadarSchemas.builds.insert(build) shouldBe 1      // renvoie la taille de l'insert

            ScalaRadarSchemas.builds.length.run shouldBe 1

            ScalaRadarSchemas.builds.first shouldBe build
        }

        db.withSession { implicit session =>
            ScalaRadarSchemas.builds.insert(build2) shouldBe 1

            ScalaRadarSchemas.builds.length.run shouldBe 2

            ScalaRadarSchemas.builds.where(_.projectName === "name").sortBy(_.endDate.desc).firstOption shouldBe Option(build2)
        }
    }

    ignore("fetch database") { // Think stop play to release lock
        Database.forURL(url = "jdbc:hsqldb:file:/Users/ugobourdon/test/db/test", user = "SA").withSession { implicit session =>
            println(ScalaRadarSchemas.builds.length.run)
        }
    }

    ignore("init database file") {
        Database.forURL(url = "jdbc:hsqldb:file:/Users/ugobourdon/test/db/test", user = "SA").withSession { implicit session =>
            ScalaRadarSchemas.builds.ddl.create
        }
    }

    /*class Db(entities: Traversable[Entity] = Set(Entity[SuccessfulBuild]()),
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
    )*/

    ignore("test sorm") {
        val expectedBuild = SuccessfulBuild("id", 123L, 321L, "", "name", "url")
        /*val db = new Db(url = "jdbc:hsqldb:mem:test")

        db.save(expectedBuild)
        db.query[SuccessfulBuild].fetchOne().map { build =>
            build.buildId       shouldBe    expectedBuild.buildId
            build.startDate     shouldBe    expectedBuild.startDate
            build.endDate       shouldBe    expectedBuild.endDate
            build.projectName   shouldBe    expectedBuild.projectName
            build.projectUrl    shouldBe    expectedBuild.projectUrl
            build.projectPath   shouldBe    expectedBuild.projectPath
        }.getOrElse(fail("return None instead of Option[SuccessfulBuild]"))*/
    }

    import org.squeryl.SessionFactory
    import org.squeryl.Schema
    import org.squeryl.Session
    import org.squeryl.adapters.H2Adapter
    import org.squeryl.PrimitiveTypeMode._

    ignore("test squeryl") {
        Class.forName("org.h2.Driver")

        SessionFactory.concreteFactory = Some( () =>
            Session.create(
                java.sql.DriverManager.getConnection("jdbc:h2:mem:test"),
                new H2Adapter
            )
        )

        val expectedBuild = SuccessfulBuild("id", 123L, 321L, "name", "url", "")

        inTransaction {
            ScalaRadarSchema.create

            val inserted = ScalaRadarSchema.builds.insert(expectedBuild)
            val result = ScalaRadarSchema.builds.single

            inserted    shouldBe    expectedBuild
            result      shouldBe    expectedBuild
        }
    }

    object ScalaRadarSchema extends Schema {
        // TODO id decorator
        val builds = table[SuccessfulBuild]("SUCCESSFUL_BUILDS")
    }
}