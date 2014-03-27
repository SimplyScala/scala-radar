package dao

import model.SuccessfulBuild
import scala.slick.driver.JdbcDriver.Backend
import dao.schema.BuildSchema
import scala.slick.driver.HsqldbDriver.simple._              // Not remove

trait Dao {
    type ObjectId = Long

    def save(build: SuccessfulBuild)(implicit db: Backend#DatabaseDef): Unit
    def retrieveLastBuild(projectName: String)(implicit db: Backend#DatabaseDef): Option[SuccessfulBuild]
}

object Dao extends Dao {
    def save(build: SuccessfulBuild)(implicit db: Backend#DatabaseDef): Unit = db.withSession { implicit session =>
        BuildSchema.builds.insert(build)
    }

    def retrieveLastBuild(projectName: String)(implicit db: Backend#DatabaseDef): Option[SuccessfulBuild] = db.withSession { implicit session =>
        BuildSchema.builds.where(_.projectName === projectName).sortBy(_.endDate.desc).firstOption
    }
}

/* SORM VERSION

trait Dao {
    type ObjectId = Long

    def save(build: SuccessfulBuild)(implicit db: sorm.Instance): ObjectId
    def retrieveLastBuild()(implicit db: sorm.Instance): Option[SuccessfulBuild]
}

object Dao extends Dao {
    def save(build: SuccessfulBuild)(implicit db: sorm.Instance): ObjectId = db.save(build).id

    def retrieveLastBuild()(implicit db: sorm.Instance): Option[SuccessfulBuild with Persisted] =
        db.query[SuccessfulBuild].order("endDate", true).fetchOne()
}*/