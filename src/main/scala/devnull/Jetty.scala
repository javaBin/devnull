package devnull

import java.io.File
import java.time.Clock

import com.typesafe.scalalogging.LazyLogging
import devnull.sessions.{
  CachingSessionService,
  SessionService,
  SleepingPillHttpSessionClient
}
import devnull.storage._
import doobie.hikari.hikaritransactor.HikariTransactor
import unfiltered.jetty.Server

import scala.util.Properties._
import scalaz.concurrent.Task

case class AppConfig(
    httpPort: Int,
    home: File,
    sessionEndedCheck: Boolean,
    databaseConfig: DatabaseConfig,
    sleepingPillUrl: String
)

case class AppReference(server: Server)

object Jetty extends InitApp[AppConfig, AppReference] {

  override def onStartup(): AppConfig = {
    val config: AppConfig = createConfig()
    logger.info(s"Using config $config")
    logger.info(s"Buildinfo ${devnull.BuildInfo}")

    Migration.runMigration(config.databaseConfig)
    config
  }

  override def onStart(cfg: AppConfig): AppReference = {
    val dbCfg: DatabaseConfig = cfg.databaseConfig
    val xa = for {
      xa <- HikariTransactor[Task](
             dbCfg.driver,
             dbCfg.connectionUrl,
             dbCfg.username,
             dbCfg.password.value
           )
      _ <- xa.configure(
            hxa =>
              Task.delay {
                hxa.setMaximumPoolSize(10)
            }
          )
    } yield xa

    val repository: FeedbackRepository                   = new FeedbackRepository()
    val paperFeedbackRepository: PaperFeedbackRepository = new PaperFeedbackRepository()
    implicit val clock: Clock                            = Clock.systemUTC()
    val emsService: SessionService = new CachingSessionService(
      new SleepingPillHttpSessionClient(cfg.sleepingPillUrl),
      cfg.sessionEndedCheck
    )

    val server = Server
      .http(cfg.httpPort)
      .plan(
        Resources(emsService, repository, paperFeedbackRepository, xa.unsafePerformSync)
      )
      .requestLogging("access.log")

    server.run(_ => logger.info("Running server at " + cfg.httpPort))
    AppReference(server)
  }

  override def onShutdown(refs: AppReference): Unit = {}

  def createConfig(): AppConfig = {
    val port = envOrElse("PORT", "8082").toInt
    val home = new File(propOrElse("app.home", envOrElse("app.home", ".")))
    val sleepingPillUrl = propOrNone("sleepingPillUrl").getOrElse(
      envOrElse("SLEEPING_PILL_URL", "https://test-sleepingpill.javazone.no")
    )
    val sessionEndedCheck = envOrElse("SESSION_ENDED_CHECK", "true").toBoolean

    val dbConfig = DatabaseConfigEnv()
    AppConfig(port, home, sessionEndedCheck, dbConfig, sleepingPillUrl)
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
