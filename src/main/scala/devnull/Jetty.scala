package devnull

import java.io.File

import devnull.storage.{FeedbackRepository, DatabaseConfig, Migration}
import doobie.util.transactor.DriverManagerTransactor
import unfiltered.jetty.Server

import scala.util.Properties._
import scalaz.concurrent.Task

case class AppConfig(httpPort: Int, httpContextPath: String, home: File, databaseConfig: DatabaseConfig)

case class AppReference(server: Server)

object Jetty extends InitApp[AppConfig, AppReference] {


  override def onStartup(): AppConfig = {
    val config: AppConfig = createConfig()
    Migration.runMigration(config.databaseConfig)
    config
  }

  override def onStart(cfg: AppConfig): AppReference = {
    val dbCfg: DatabaseConfig = cfg.databaseConfig
    val xa = DriverManagerTransactor[Task](dbCfg.driver, dbCfg.connectionUrl, dbCfg.username, dbCfg.password)

    val repository: FeedbackRepository = new FeedbackRepository()

    val server = unfiltered.jetty.Server.http(cfg.httpPort).context(cfg.httpContextPath) {
      _.plan(Resources(repository, xa))
    }.requestLogging("access.log")

    server.underlying.setSendDateHeader(true)
    server.run(_ => println("Running server at " + cfg.httpPort))
    AppReference(server)
  }

  override def onShutdown(refs: AppReference): Unit = {}

  def createConfig(): AppConfig = {
    val port = envOrElse("PORT", "8082").toInt
    val contextPath = propOrElse("contextPath", envOrElse("CONTEXT_PATH", "/server"))
    val home = new File(propOrElse("app.home", envOrElse("app.home", ".")))

    val dbConfig = DatabaseConfig(
      database = propOrElse("dbName", envOrElse("DB_NAME", "devnull")),
      username = propOrElse("dbUsername", envOrElse("DB_USERNAME", "devnull")),
      password = propOrElse("dbPassword", envOrElse("DB_PASSWORD", "devnull"))
    )

    AppConfig(port, contextPath, home, dbConfig)
  }

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