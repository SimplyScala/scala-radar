package dao

import org.scalatest.{Matchers, FunSuite}
import model.SuccessfulBuild
import scala.slick.driver.HsqldbDriver.simple._
import dao.schema.ScalaRadarSchemas

class DaoTest extends FunSuite with Matchers {
    test("[slick] should save SuccessfulBuild") {
        val expectedBuild = SuccessfulBuild("buildId", 123L, 321L, "path", "name", "url")

        implicit val db = Database.forURL(url = "jdbc:hsqldb:mem:test1", user = "SA")

        db.withSession { implicit session => ScalaRadarSchemas.builds.ddl.create }

        Dao.save(expectedBuild)

        db.withSession { implicit session =>
            ScalaRadarSchemas.builds.length.run shouldBe 1
            ScalaRadarSchemas.builds.first shouldBe expectedBuild
        }
    }

    test("[slick] should retrieve last build") {
        val build1 = SuccessfulBuild("buildId", 0L, 1L, "path", "name", "url")
        val build2 = SuccessfulBuild("buildId2", 0L, 2L, "path", "name", "url")

        implicit val db = Database.forURL(url = "jdbc:hsqldb:mem:test2", user = "SA")

        db.withSession { implicit session => ScalaRadarSchemas.builds.ddl.create }

        Dao.save(build1); Dao.save(build2)

        Dao.retrieveLastBuild("name") shouldBe Option(build2)
    }

    /*test("[sorm] should save SuccessfulBuild") {
        memoryDbFixture { db =>
            implicit val idb = db

            val expectedBuild = SuccessfulBuild("buildId", 123L, 321L, "name", "url", "path")

            // When
            Dao.save(expectedBuild) shouldBe 1L

            // Then
            db.query[SuccessfulBuild].fetchOne() map { build =>
                build.buildId       shouldBe    expectedBuild.buildId
                build.projectName   shouldBe    build.projectName
            } getOrElse { fail("retrieve None instead of Some(SuccessfulBuild)") }
        }
    }

    test("[sorm] should retrieve last build") {
        memoryDbFixture { db =>
            implicit val idb = db

            val build1 = SuccessfulBuild("buildId", 0L, 1L, "name", "url", "path")
            val build2 = SuccessfulBuild("buildId", 0L, 2L, "name", "url", "path")

            db.save(build1)
            db.save(build2)

            // When
            Dao.retrieveLastBuild() map { build =>

            // Then
                build.projectName   shouldBe    "name"
                build.endDate       shouldBe    2L
            } getOrElse { fail("retrieve None instead of Some(SuccessfulBuild)") }
        }
    }

    def memoryDbFixture(fixture: Db => Unit) {
        val db = new Db(url = "jdbc:h2:mem:test")
        Try { fixture(db) } match { case _ => Option(db) foreach(_.close()) }
    }*/
}