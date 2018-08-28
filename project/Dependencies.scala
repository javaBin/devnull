import sbt._

object Dependencies {

  val ProjectResolvers = Seq(
    "tpolecat" at "http://dl.bintray.com/tpolecat/maven",
    "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
    Resolver.bintrayRepo("ingarabr", "oss-external")
  )

  private lazy val unfilteredVersion = "0.9.1"

  val joda = Seq(
    "joda-time" % "joda-time" % "2.10",
    "org.joda" % "joda-convert" % "2.1.1"
  )

  val unfiltered = Seq(
    "ws.unfiltered"           %% "unfiltered-filter"     % unfilteredVersion,
    "ws.unfiltered"           %% "unfiltered-directives" % unfilteredVersion,
    "ws.unfiltered"           %% "unfiltered-jetty"      % unfilteredVersion,
    "no.arktekk"              %% "linx"                  % "0.4",
    "io.mth"                  %% "unfiltered-cors"       % "0.4-IA1" exclude("net.databinder", "*"),
    "org.dispatchhttp"        %% "dispatch-core"         % "0.14.0",
    "org.json4s"              %% "json4s-native"         % "3.5.4"
  )

  val logging = Seq(
    "org.slf4j"                  %  "slf4j-api"             % "1.7.25",
    "org.slf4j"                  %  "slf4j-simple"          % "1.7.25",
    "com.typesafe.scala-logging" %% "scala-logging"         % "3.9.0"
  )

  lazy val doobieVersion = "0.4.4"

  val database = Seq(
    "org.flywaydb"   %  "flyway-core"      % "4.2.0",
    "org.tpolecat"   %% "doobie-core"      % doobieVersion  withSources(),
    "org.tpolecat"   %% "doobie-postgres"  % doobieVersion  withSources()  exclude("postgresql", "postgresql"),
    "org.tpolecat"   %% "doobie-hikari"    % doobieVersion  withSources(),
    "org.tpolecat"   %% "doobie-scalatest" % doobieVersion  withSources(),
    "com.zaxxer"     %  "HikariCP"         % "3.2.0"
  )

  val caffine =    "com.github.ben-manes.caffeine" %  "caffeine"              % "2.6.2"
  val scalaTest =   "org.scalatest"                %%  "scalatest"            % "3.0.5"   % Test

}
