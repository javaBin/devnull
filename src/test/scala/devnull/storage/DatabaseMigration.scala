package devnull.storage

import org.scalatest.{BeforeAndAfterAll, Suite}

trait DatabaseMigration extends BeforeAndAfterAll {
  this: Suite =>

  def cfg: DatabaseConfig

  override protected def beforeAll() {
    Migration.runMigration(cfg)
  }
}
