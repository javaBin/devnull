package devnull

import devnull.storage.{DatabaseConfig, Migration}
import unfiltered.jetty.Server

case class AppConfig(httpPort: Int, httpContextPath: String, databaseConfig: DatabaseConfig)

case class AppReference(server: Server)

object Jetty extends InitApp[AppConfig, AppReference] {

  override def onStartup(): AppConfig = {
    val config: AppConfig = AppConfig(8080, "/server", DatabaseConfig())
    Migration.runMigration(config.databaseConfig)
    config
  }

  override def onStart(cfg: AppConfig): AppReference = {
    val server = unfiltered.jetty.Server.http(cfg.httpPort).context(cfg.httpContextPath) {
      _.plan(Resources())
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