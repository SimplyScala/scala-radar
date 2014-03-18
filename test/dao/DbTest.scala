package dao

import org.scalatest.{Matchers, FunSuite}
import model.{Project, SuccessfulBuild}
import scalax.file.Path

class DbTest extends FunSuite with Matchers {

    test("test db") {
        val expectedBuild = SuccessfulBuild("id", 123L, 321L, "name", "url", "")
        val db = new Db(url = "jdbc:h2:mem:test")

        db.save(expectedBuild)
        db.query[SuccessfulBuild].fetchOne().map { build =>
            build.buildId       shouldBe    expectedBuild.buildId
            build.startDate     shouldBe    expectedBuild.startDate
            build.endDate       shouldBe    expectedBuild.endDate
            build.projectName   shouldBe    expectedBuild.projectName
            build.projectUrl    shouldBe    expectedBuild.projectUrl
            build.projectPath   shouldBe    expectedBuild.projectPath
        }.getOrElse(fail("return None instead of Option[SuccessfulBuild]")) 
    }
}