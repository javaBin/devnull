import sbt._

object Dependencies {

  private lazy val unfilteredVersion = "0.9.1"

  val joda = Seq(
    "joda-time" % "joda-time" % "2.9.9",
    "org.joda" % "joda-convert" % "1.9.2"
  )

  val unfiltered = Seq(
    "ws.unfiltered"           %% "unfiltered-filter"     % unfilteredVersion,
    "ws.unfiltered"           %% "unfiltered-directives" % unfilteredVersion,
    "ws.unfiltered"           %% "unfiltered-jetty"      % unfilteredVersion,
    "no.arktekk"              %% "linx"                  % "0.4",
    "io.mth"                  %% "unfiltered-cors"       % "0.3" exclude("net.databinder", "*"),
    "net.databinder.dispatch" %% "dispatch-core"         % "0.13.3",
    "org.json4s"              %% "json4s-native"         % "3.5.3"
  )

  val logging = Seq(
    "org.slf4j"                  %  "slf4j-api"             % "1.7.25",
    "org.slf4j"                  %  "slf4j-simple"          % "1.7.25",
    "com.typesafe.scala-logging" %% "scala-logging"         % "3.7.2"
  )

  lazy val doobieVersion = "0.4.4"

  val database = Seq(
    "org.flywaydb"   %  "flyway-core"      % "4.2.0",
    "org.tpolecat"   %% "doobie-core"      % doobieVersion  withSources(),
    "org.tpolecat"   %% "doobie-postgres"  % doobieVersion  withSources()  exclude("postgresql", "postgresql"),
    "org.tpolecat"   %% "doobie-hikari"    % doobieVersion  withSources(),
    "org.tpolecat"   %% "doobie-scalatest" % doobieVersion  withSources(),
    "com.zaxxer"     %  "HikariCP"         % "2.7.6"
  )

  val caffine =    "com.github.ben-manes.caffeine" %  "caffeine"              % "2.6.1"
  val scalaTest =   "org.scalatest"                %%  "scalatest"            % "3.0.4"   % Test

}
