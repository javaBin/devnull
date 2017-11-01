package devnull

import java.io.File
import java.time.Clock

import com.typesafe.scalalogging.LazyLogging
import devnull.sessions.{CachingSessionService, EmsHttpSessionClient, SessionService, SleepingPillHttpSessionClient}
import devnull.storage._
import doobie.contrib.hikari.hikaritransactor.HikariTransactor
import unfiltered.jetty.Server

import scala.util.Properties._
import scalaz.concurrent.Task

case class AppConfig(
    httpPort: Int,
    httpContextPath: String,
    home: File,
    databaseConfig: DatabaseConfig,
    emsUrl: String,
    sleepingPillUrl: Option[String]
)

case class AppReference(server: Server)

object Jetty extends InitApp[AppConfig, AppReference] {


  override def onStartup(): AppConfig = {
    val config: AppConfig = createConfig()
    logger.info(s"Using config $config")
    Migration.runMigration(config.databaseConfig)
    config
  }

  override def onStart(cfg: AppConfig): AppReference = {
    val dbCfg: DatabaseConfig = cfg.databaseConfig
    val xa = for {
      xa <- HikariTransactor[Task](dbCfg.driver, dbCfg.connectionUrl, dbCfg.username, dbCfg.password.value)
      _ <- xa.configure(hxa =>
        Task.delay {
          hxa.setMaximumPoolSize(10)
        })
    } yield xa

    val repository: FeedbackRepository = new FeedbackRepository()
    val paperFeedbackRepository: PaperFeedbackRepository = new PaperFeedbackRepository()
    implicit val clock = Clock.systemUTC()
    val emsService: SessionService = new CachingSessionService(
      cfg.sleepingPillUrl.map(url => new SleepingPillHttpSessionClient(url))
          .getOrElse(new EmsHttpSessionClient(cfg.emsUrl))
    )

    val server = unfiltered.jetty.Server.http(cfg.httpPort).context(cfg.httpContextPath) {
      _.plan(Resources(emsService, repository, paperFeedbackRepository, xa.unsafePerformSync))
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
    val sleepingPillUrl = propOrNone("sleepingPillUrl").orElse(envOrNone("SLEEPING_PILL_URL"))

    val dbConfig = DatabaseConfigEnv()
    AppConfig(port, contextPath, home, dbConfig, emsUrl, sleepingPillUrl)
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