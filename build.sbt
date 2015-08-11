import scala.util.Properties

val commonSettings = Seq(
  organization := "no.java.devnull",
  scalaVersion := "2.11.6",
  name := "devnull",
  crossScalaVersions := Seq("2.11.6"),
  scalacOptions := Seq("-deprecation", "-feature"),
  pomIncludeRepository := {
    x => false
  },
  crossPaths := false,
  publishTo <<= (version) apply {
    (v: String) => if (v.trim().endsWith("SNAPSHOT")) {
      Some("JavaBin Nexus repo" at "http://nye.java.no/nexus/content/repositories/snapshots")
    }
    else {
      Some("JavaBin Nexus repo" at "http://nye.java.no/nexus/content/repositories/releases")
    }
  },
  credentials ++= {
    val cred = Path.userHome / ".sbt" / "javabin.credentials"
    if (cred.exists) Seq(Credentials(cred)) else Nil
  },
  target in App := target.value / "appmgr" / "root",
  packageBin in Appmgr <<= (packageBin in Appmgr).dependsOn(packageBin in App),
  appmgrLauncher in Appmgr := (appmgrLauncher in Appmgr).value.map(_.copy(command = "jetty", name = "devnull")),
  aether.AetherKeys.aetherArtifact <<= (aether.AetherKeys.aetherArtifact, (packageBin in Appmgr)) map { (art, build) =>
    art.attach(build, "appmgr", "zip")
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
  "joda-time" % "joda-time" % "2.2",
  "org.joda" % "joda-convert" % "1.2"
)

lazy val unfilteredVersion = "0.8.3"

val unfiltered = Seq(
  "net.databinder" %% "unfiltered-filter"     % unfilteredVersion,
  "net.databinder" %% "unfiltered-directives" % unfilteredVersion,
  "net.databinder" %% "unfiltered-jetty"      % unfilteredVersion,
  "com.jteigen"    %% "linx"                  % "0.2",
  "org.slf4j"      %  "slf4j-api"             % "1.7.7",
  "org.slf4j"      %  "slf4j-simple"          % "1.7.7"
)

lazy val doobieVersion = "0.2.1"

val database = Seq(
  "org.flywaydb"   %  "flyway-core"               % "3.2.1",
  "org.tpolecat"   %% "doobie-core"               % doobieVersion  withSources(),
  "org.tpolecat"   %% "doobie-contrib-postgresql" % doobieVersion  withSources()  exclude("postgresql", "postgresql")
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
    libraryDependencies ++= joda ++ unfiltered ++ database ++ Seq(
      "org.json4s"                 %% "json4s-native"         % "3.2.10",
      "org.ini4j"                  %  "ini4j"                 % "0.5.2",
      "org.constretto"             %% "constretto-scala"      % "1.1",
      "net.hamnaberg.rest"         %% "scala-json-collection" % "2.3",
      "commons-io"                 %  "commons-io"            % "2.3",
      "com.typesafe.scala-logging" %% "scala-logging"         % "3.1.0",
      "org.scalatest"              %%  "scalatest"            % "2.2.4"   % "test"
    )
    /*
    Arguments: http://www.scalatest.org/user_guide/using_the_runner
    -l   exclude tag
    -n   include tag
    -w   package with sub packages
    */
  )
