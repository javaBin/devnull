import sbt._

object Dependencies {

  private lazy val unfilteredVersion = "0.8.4"

  val joda = Seq(
    "joda-time" % "joda-time" % "2.9.9",
    "org.joda" % "joda-convert" % "1.9.2"
  )

  val unfiltered = Seq(
    "net.databinder"          %% "unfiltered-filter"     % unfilteredVersion,
    "net.databinder"          %% "unfiltered-directives" % unfilteredVersion,
    "net.databinder"          %% "unfiltered-jetty"      % unfilteredVersion,
    "com.jteigen"             %% "linx"                  % "0.2",
    "io.mth"                  %% "unfiltered-cors"       % "0.3",
    "net.databinder.dispatch" %% "dispatch-core"         % "0.13.2",
    "org.json4s"              %% "json4s-native"         % "3.5.3",
    "net.hamnaberg.rest"      %% "scala-json-collection" % "2.4"
  )

  val logging = Seq(
    "org.slf4j"                  %  "slf4j-api"             % "1.7.25",
    "org.slf4j"                  %  "slf4j-simple"          % "1.7.25",
    "com.typesafe.scala-logging" %% "scala-logging"         % "3.7.2"
  )

  lazy val doobieVersion = "0.3.0"

  val database = Seq(
    "org.flywaydb"   %  "flyway-core"               % "4.2.0",
    "org.tpolecat"   %% "doobie-core"               % doobieVersion  withSources(),
    "org.tpolecat"   %% "doobie-contrib-postgresql" % doobieVersion  withSources()  exclude("postgresql", "postgresql"),
    "org.tpolecat"   %% "doobie-contrib-hikari"     % doobieVersion  withSources(),
    "com.zaxxer"     %  "HikariCP"                  % "2.7.2"
  )

  val caffine =    "com.github.ben-manes.caffeine" %  "caffeine"              % "2.5.6"
  val scalaTest =   "org.scalatest"                %%  "scalatest"            % "3.0.4"   % Test

}
