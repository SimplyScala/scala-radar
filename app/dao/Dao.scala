package dao

import model.SuccessfulBuild
import sorm.Persisted

trait Dao {
    type ObjectId = Long

    // TODO parameterize - be careful T <: AnyRef
    def save(build: SuccessfulBuild)(implicit db: sorm.Instance): ObjectId

    def retrieveLastBuild()(implicit db: sorm.Instance): Option[SuccessfulBuild]
    //def save[T <: scala.AnyRef](obj: T)(db: sorm.Instance): ObjectId
    //def retrieve[T](clazz: Class[T])
}

object Dao extends Dao {
    def save(build: SuccessfulBuild)(implicit db: sorm.Instance): ObjectId = db.save(build).id

    def retrieveLastBuild()(implicit db: sorm.Instance): Option[SuccessfulBuild with Persisted] =
        db.query[SuccessfulBuild].order("endDate", true).fetchOne()
}