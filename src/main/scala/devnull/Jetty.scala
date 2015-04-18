package devnull

import devnull.storage.{FeedbackRepository, DatabaseConfig, Migration}
import doobie.util.transactor.DriverManagerTransactor
import unfiltered.jetty.Server

import scalaz.concurrent.Task

case class AppConfig(httpPort: Int, httpContextPath: String, databaseConfig: DatabaseConfig)

case class AppReference(server: Server)

object Jetty extends InitApp[AppConfig, AppReference] {

  override def onStartup(): AppConfig = {
    val config: AppConfig = AppConfig(8082, "/server", DatabaseConfig())
    Migration.runMigration(config.databaseConfig)
    config
  }

  override def onStart(cfg: AppConfig): AppReference = {
    val xa = DriverManagerTransactor[Task](cfg.databaseConfig.driver, cfg.databaseConfig.connectionUrl, cfg.databaseConfig.username, cfg.databaseConfig.password)

    val repository: FeedbackRepository = new FeedbackRepository()

    val server = unfiltered.jetty.Server.http(cfg.httpPort).context(cfg.httpContextPath) {
      _.plan(Resources(repository))
    }.requestLogging("access.log")

    server.underlying.setSendDateHeader(true)
    server.run(_ => println("Running server at " + cfg.httpPort))
    AppReference(server)
  }

  override def onShutdown(refs: AppReference): Unit = {}

}

trait InitApp[C, R] extends App {

  def onStartup(): C

  def onStart(cfg: C): R

  def onShutdown(refs: R)

  println("onStartup")
  val cfg = onStartup()
  println("onStart")
  val refs = onStart(cfg)
  println("onShutdown")
  onShutdown(refs)
}