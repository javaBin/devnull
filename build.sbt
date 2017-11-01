import scala.util.Properties
import aether.AetherKeys._

val commonSettings = Seq(
  organization := "no.java.devnull",
  scalaVersion := "2.11.11",
  name := "devnull",
  crossScalaVersions := Seq("2.11.11"),
  scalacOptions := Seq("-deprecation", "-feature"),
  pomIncludeRepository := {
    x => false
  },
  crossPaths := false,
  publishTo := {
    if (isSnapshot.value) {
      Some("JavaBin Nexus repo" at "http://nye.java.no/nexus/content/repositories/snapshots")
    } else {
      Some("JavaBin Nexus repo" at "http://nye.java.no/nexus/content/repositories/releases")
    }
  },
  credentials ++= {
    val cred = Path.userHome / ".sbt" / "javabin.credentials"
    if (cred.exists) Seq(Credentials(cred)) else Nil
  },
  target in App := target.value / "appmgr" / "root",
  packageBin in Appmgr := (packageBin in Appmgr).dependsOn(packageBin in App).value,
  appmgrLauncher in Appmgr := (appmgrLauncher in Appmgr).value.map(_.copy(command = "jetty", name = "devnull")),
  aetherArtifact := {
    val artifact = aetherArtifact.value
    artifact.attach((packageBin in Appmgr).value, "appmgr", "zip")
  },
  credentials ++= {
    (for {
    username <- Properties.envOrNone("NEXUS_USERNAME")
    password <- Properties.envOrNone("NEXUS_PASSWORD")
  } yield
    Credentials(
      "Sonatype Nexus Repository Manager",
      "nye.java.no",
      username,
      password
    )).toSeq
  }

) ++ overridePublishBothSettings

val joda = Seq(
  "joda-time" % "joda-time" % "2.9.9",
  "org.joda" % "joda-convert" % "1.9.2"
)

lazy val unfilteredVersion = "0.8.4"

val unfiltered = Seq(
  "net.databinder" %% "unfiltered-filter"     % unfilteredVersion,
  "net.databinder" %% "unfiltered-directives" % unfilteredVersion,
  "net.databinder" %% "unfiltered-jetty"      % unfilteredVersion,
  "com.jteigen"    %% "linx"                  % "0.2",
  "org.slf4j"      %  "slf4j-api"             % "1.7.25",
  "org.slf4j"      %  "slf4j-simple"          % "1.7.25",
  "io.mth"         %% "unfiltered-cors"       % "0.3"
)

lazy val doobieVersion = "0.3.0"

val database = Seq(
  "org.flywaydb"   %  "flyway-core"               % "4.2.0",
  "org.tpolecat"   %% "doobie-core"               % doobieVersion  withSources(),
  "org.tpolecat"   %% "doobie-contrib-postgresql" % doobieVersion  withSources()  exclude("postgresql", "postgresql"),
  "org.tpolecat"   %% "doobie-contrib-hikari"     % doobieVersion  withSources(),
  "com.zaxxer"     %  "HikariCP"                  % "2.7.2"
)

lazy val AllTests = config("all") extend Test
lazy val DbTests = config("db") extend Test
def testArg(key: String, value: String ) = Tests.Argument(TestFrameworks.ScalaTest, key, value)

lazy val devnull = (project in file(".")).
  configs(AllTests, DbTests).
  settings(commonSettings).
  settings(inConfig(AllTests)(Defaults.testTasks)).
  settings(inConfig(DbTests)(Defaults.testTasks)).
  settings(Revolver.settings).
  settings(
    resolvers ++= Seq(
    "tpolecat" at "http://dl.bintray.com/tpolecat/maven",
    "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"
    ),
    testOptions in Test     := Seq(testArg("-l", "devnull.tag.db"), testArg( "-l", "devnull.tag.slow")),
    testOptions in DbTests  := Seq(testArg("-n", "devnull.tag.db")),
    testOptions in AllTests := Seq(),
    parallelExecution in DbTests := false,
    parallelExecution in AllTests := false,
    libraryDependencies ++= joda ++ unfiltered ++ database ++ Seq(
      "org.json4s"                    %% "json4s-native"         % "3.5.3",
      "org.ini4j"                     %  "ini4j"                 % "0.5.4",
      "org.constretto"                %% "constretto-scala"      % "1.2",
      "net.hamnaberg.rest"            %% "scala-json-collection" % "2.4",
      "commons-io"                    %  "commons-io"            % "2.6",
      "com.github.ben-manes.caffeine" %  "caffeine"              % "2.5.6",
      "com.google.code.findbugs"      %  "jsr305"                % "3.0.2"   % Provided,
      "net.databinder.dispatch"       %% "dispatch-core"         % "0.13.2",
      "com.typesafe.scala-logging"    %% "scala-logging"         % "3.7.2",
      "org.scalatest"                 %%  "scalatest"            % "3.0.4"   % Test
    )
    /*
    Arguments: http://www.scalatest.org/user_guide/using_the_runner
    -l   exclude tag
    -n   include tag
    -w   package with sub packages
    */
  )
enablePlugins(BuildInfoPlugin)

buildInfoPackage := "devnull"

buildInfoKeys := Seq[BuildInfoKey](
  scalaVersion,
  BuildInfoKey.action("version") { (version in ThisBuild ).value },
  BuildInfoKey.action("buildTime") { System.currentTimeMillis },
  BuildInfoKey.action("branch"){ Git.branch },
  BuildInfoKey.action("sha"){ Git.sha }
)
