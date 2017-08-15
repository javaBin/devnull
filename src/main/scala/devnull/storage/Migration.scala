package devnull.storage

import com.typesafe.scalalogging.LazyLogging
import org.flywaydb.core.Flyway
import org.flywaydb.core.internal.util.jdbc.DriverDataSource

object Migration extends LazyLogging {

  def runMigration(cfg: DatabaseConfig) = {
    val fw = new Flyway()
    val ds = new DriverDataSource(
      Thread.currentThread.getContextClassLoader,
      cfg.driver,
      cfg.connectionUrl,
      cfg.username,
      cfg.password.value
    )
    fw.setDataSource(ds)
    val numMigrationExecuted: Int = fw.migrate()
    logger.info(s"Migration scripts executed: $numMigrationExecuted")
  }

}
