package devnull.storage

import doobie.util.transactor.DriverManagerTransactor

import scalaz.concurrent.Task

object Storage {

  private val config: DatabaseConfig = new DatabaseConfig()
  val xa = DriverManagerTransactor[Task](config.driver, config.connectionUrl, config.username, config.password.value)

}
