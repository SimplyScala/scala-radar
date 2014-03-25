package dao

import model.SuccessfulBuild
import sorm.Persisted

trait Dao {
    type ObjectId = Long

    def save(build: SuccessfulBuild)(implicit db: sorm.Instance): ObjectId

    def retrieveLastBuild()(implicit db: sorm.Instance): Option[SuccessfulBuild]
}

object Dao extends Dao {
    def save(build: SuccessfulBuild)(implicit db: sorm.Instance): ObjectId = db.save(build).id

    def retrieveLastBuild()(implicit db: sorm.Instance): Option[SuccessfulBuild with Persisted] =
        db.query[SuccessfulBuild].order("endDate", true).fetchOne()
}