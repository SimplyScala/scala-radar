package dao

import org.scalatest.{Matchers, FunSuite}
import model.SuccessfulBuild
import scala.util.Try

class DaoTest extends FunSuite with Matchers {

    test("should save SuccessfulBuild") {
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

    test("should retrieve last build") {
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
    }
}