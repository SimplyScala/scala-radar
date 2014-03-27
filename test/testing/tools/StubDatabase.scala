package testing.tools

import dao.Database
import scala.slick.driver.JdbcDriver._
import org.scalatest.mock.MockitoSugar

trait StubDatabase extends Database with MockitoSugar {
    implicit val db: Backend#DatabaseDef = mock[Backend#DatabaseDef]
}