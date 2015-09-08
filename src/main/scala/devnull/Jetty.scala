package devnull

import java.io.File
import java.time.Clock

import com.typesafe.scalalogging.LazyLogging
import devnull.ems.{EmsHttpClient, CachingEmsService, EmsService}
import devnull.storage.{DatabaseConfigEnv, FeedbackRepository, DatabaseConfig, Migration}
import doobie.util.transactor.DriverManagerTransactor
import unfiltered.jetty.Server

import scala.util.Properties._
import scalaz.concurrent.Task

case class AppConfig(
    httpPort: Int,
    httpContextPath: String,
    home: File,
    databaseConfig: DatabaseConfig,
    emsUrl: String)

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
    implicit val clock = Clock.systemUTC()
    val emsService: EmsService = new CachingEmsService(new EmsHttpClient(cfg.emsUrl))

    val server = unfiltered.jetty.Server.http(cfg.httpPort).context(cfg.httpContextPath) {
      _.plan(Resources(emsService, repository, xa))
    }.requestLogging("access.log")

    server.underlying.setSendDateHeader(true)
    server.run(_ => logger.info("Running server at " + cfg.httpPort))
    AppReference(server)
  }

  override def onShutdown(refs: AppReference): Unit = {}

  def createConfig(): AppConfig = {
    val port = envOrElse("PORT", "8082").toInt
    val contextPath = propOrElse("contextPath", envOrElse("CONTEXT_PATH", "/server"))
    val home = new File(propOrElse("app.home", envOrElse("app.home", ".")))
    val emsUrl = propOrElse("emsUrl", envOrElse("EMS_URL", "http://test.javazone.no/ems/server/"))

    val dbConfig = DatabaseConfigEnv()
    AppConfig(port, contextPath, home, dbConfig, emsUrl)
  }

}

trait InitApp[C, R] extends App with LazyLogging {

  def onStartup(): C

  def onStart(cfg: C): R

  def onShutdown(refs: R)

  logger.debug("onStartup")
  val cfg = onStartup()
  logger.debug("onStart")
  val refs = onStart(cfg)
  logger.debug("onShutdown")
  onShutdown(refs)
}