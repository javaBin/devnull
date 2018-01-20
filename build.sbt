import scala.util.Properties
import aether.AetherKeys._
import Dependencies._

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
    libraryDependencies ++= joda ++ unfiltered ++ database ++ logging ++
        Seq(caffine, scalaTest)
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
